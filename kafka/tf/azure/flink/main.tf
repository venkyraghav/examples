terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.54.0"
    }
  }
}

locals {
  cloud  = "AZURE"
  region = "eastus"
}

variable "oauth_external_token_url" {
  description = "The OAuth token URL from Microsoft Azure Entra ID as the identity provider"
  type        = string
}

variable "oauth_external_client_id" {
  description = "The OAuth token client id from Microsoft Azure Entra ID as the identity provider"
  type        = string
}

variable "oauth_external_client_secret" {
  description = "The OAuth token client secret from Microsoft Azure Entra ID as the identity provider"
  type        = string
  sensitive   = true
}

variable "oauth_external_token_scope" {
  description = "(Required field for Azure Entra ID) The OAuth client application scope from Microsoft Azure Entra ID as the identity provider"
  type        = string
}

variable "oauth_identity_pool_id" {
  description = "The OAuth identity pool id from Microsoft Azure Entra ID as the identity provider, registered with Confluent Cloud"
  type        = string
}

provider "confluent" {
  oauth {
    oauth_external_token_url     = var.oauth_external_token_url
    oauth_external_client_id     = var.oauth_external_client_id
    oauth_external_client_secret = var.oauth_external_client_secret
    oauth_identity_pool_id       = var.oauth_identity_pool_id
    oauth_external_token_scope   = var.oauth_external_token_scope
  }
}

data "confluent_organization" "main" {}

data "confluent_environment" "venky" {
  id = "env-mzn3gw"
}

# Update the config to use a cloud provider and region of your choice.
# https://registry.terraform.io/providers/confluentinc/confluent/latest/docs/resources/confluent_kafka_cluster
data "confluent_kafka_cluster" "standard" {
  id = "lkc-pzwy9y"
  environment {
    id = data.confluent_environment.venky.id
  }
}

data "confluent_schema_registry_cluster" "main" {
  environment {
    id = data.confluent_environment.venky.id
  }
}

data "confluent_flink_region" "us-east" {
  cloud  = local.cloud
  region = local.region
}

data "confluent_flink_compute_pool" "main" {
  id = "lfcp-8pvoom"
  environment {
    id = data.confluent_environment.venky.id
  }
}

resource "confluent_flink_statement" "read-orders-source-table" {
  organization {
    id = data.confluent_organization.main.id
  }
  environment {
    id = data.confluent_environment.venky.id
  }
  compute_pool {
    id = data.confluent_flink_compute_pool.main.id
  }
  principal {
    id = var.oauth_identity_pool_id
  }
  # https://docs.confluent.io/cloud/current/flink/reference/example-data.html#marketplace-database
  statement = "insert into venky.venky_standard_cluster.topic1B SELECT * FROM venky.venky_standard_cluster.topic1A;"
  properties = {
    "sql.current-catalog"  = data.confluent_environment.venky.display_name
    "sql.current-database" = data.confluent_kafka_cluster.standard.display_name
  }
  rest_endpoint = data.confluent_flink_region.us-east.rest_endpoint
}

#
# DeveloperRead on sources
resource "confluent_role_binding" "src_read" {
  for_each    = toset(var.src_topics)
  principal   = "User:${var.identity_pool_id}" # identity pool as principal
  role_name   = "DeveloperRead"
  crn_pattern = "${confluent_kafka_cluster.main.rbac_crn}/kafka=${confluent_kafka_cluster.main.id}/topic=${each.key}"
}

# DeveloperWrite on sinks
resource "confluent_role_binding" "sink_write" {
  for_each    = toset(var.sink_topics)
  principal   = "User:${var.identity_pool_id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.main.rbac_crn}/kafka=${confluent_kafka_cluster.main.id}/topic=${each.key}"
}

# Flink requires Transactional-Id read/write (exactly-once)
resource "confluent_role_binding" "txn_read" {
  principal   = "User:${var.identity_pool_id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${confluent_kafka_cluster.main.rbac_crn}/kafka=${confluent_kafka_cluster.main.id}/transactional-id=_confluent-flink_*"
}

resource "confluent_role_binding" "txn_write" {
  principal   = "User:${var.identity_pool_id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.main.rbac_crn}/kafka=${confluent_kafka_cluster.main.id}/transactional-id=_confluent-flink_*"
}


# Read subjects used by sources (adjust names as needed)
resource "confluent_role_binding" "sr_read" {
  for_each    = toset(var.src_topics)
  principal   = "User:${var.identity_pool_id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${data.confluent_schema_registry_cluster.main.rbac_crn}/schema-registry=${data.confluent_schema_registry_cluster.main.id}/subject=${each.key}-value"
}

resource "confluent_flink_statement" "job" {
  organization { id = data.confluent_organization.main.id }
  environment { id = confluent_environment.env.id }
  compute_pool { id = confluent_flink_compute_pool.pool.id }

  principal { id = var.identity_pool_id } # single principal only

  statement = file("./sql/your_statement.sql")
  properties = {
    "sql.current-catalog"  = confluent_environment.env.display_name
    "sql.current-database" = confluent_kafka_cluster.main.display_name
  }
  rest_endpoint = data.confluent_flink_region.region.rest_endpoint

  depends_on = [
    confluent_role_binding.src_read,
    confluent_role_binding.sink_write,
    confluent_role_binding.txn_read,
    confluent_role_binding.txn_write
  ]
}

resource "confluent_role_binding" "txn_write" {
  principal   = "User:${var.identity_pool_id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.main.rbac_crn}/kafka=${confluent_kafka_cluster.main.id}/transactional-id=_confluent-flink_*"
}
