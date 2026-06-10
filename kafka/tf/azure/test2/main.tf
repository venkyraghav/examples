terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "=2.57.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=4.57.0"
    }
  }
}

variable "confluent_cloud_api_key" {
  type        = string
  description = "Confluent Cloud API Key"
}

variable "confluent_cloud_api_secret" {
  type        = string
  description = "Confluent Cloud API Secret"
  sensitive   = true
}

locals {
  cc_env = "env-mzn3gw"
  privateenabled = 0
  publiccluster = ["atlas"]
  kvs = ["one", "two"]

  publiccluster_kvs = {
    for pair in flatten([
      for cluster_key, cluster in confluent_kafka_cluster.public : [
        for item in local.kvs : {
          key   = cluster_key
          kv  = item
          cluster   = cluster
        }
      ]
    ]) :
    "${pair.key}-${pair.kv}" => pair
  }

  privatecluster_kvs = local.privateenabled == 0 ? {} : {
    for pair in flatten([
      for cluster_key, cluster in confluent_kafka_cluster.private : [
        for item in local.kvs : {
          key   = cluster_key
          kv  = item
          cluster   = cluster
        }
      ]
    ]) :
    "${pair.key}-${pair.kv}" => pair
  }
}

output "privatecluster_kvs" {
  value = local.privatecluster_kvs
}

output "publiccluster_kvs" {
  value = local.publiccluster_kvs
}

data "confluent_environment" "env" {
  id = local.cc_env
}

resource "confluent_service_account" "admin" {
  display_name = "venky-2-admin"
}

resource "confluent_kafka_cluster" "private" {
  count = local.privateenabled == 1 ? 1 : 0
  display_name = "private"
  availability = "SINGLE_ZONE"
  cloud        = "AZURE"
  region       = "eastus2"

  basic {
  }
  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_api_key" "private_key" {
  count = local.privateenabled == 1 ? 1 : 0

  display_name = "venky-2-test-0"
  owner {
    id = confluent_service_account.admin.id
    api_version = confluent_service_account.admin.api_version
    kind = confluent_service_account.admin.kind
  }
  managed_resource {
    id = confluent_kafka_cluster.private[0].id
    api_version = confluent_kafka_cluster.private[0].id
    kind = confluent_kafka_cluster.private[0].id
    environment {
      id = data.confluent_environment.env.id
    }
  }
  depends_on = [ confluent_kafka_cluster.private, confluent_service_account.admin ]
}

resource "confluent_kafka_cluster" "public" {
  for_each = {for item in local.publiccluster : item => item}

  display_name = "private"
  availability = "SINGLE_ZONE"
  cloud        = "AZURE"
  region       = "eastus2"

  basic {
  }
  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_api_key" "public_key" {
  for_each = confluent_kafka_cluster.public

  display_name = "venky-2-test-${each.key}"
  owner {
    id = confluent_service_account.admin.id
    api_version = confluent_service_account.admin.api_version
    kind = confluent_service_account.admin.kind
  }
  managed_resource {
    id = each.value.id
    api_version = each.value.api_version
    kind = each.value.kind
    environment {
      id = data.confluent_environment.env.id
    }
  }
  depends_on = [ confluent_kafka_cluster.public, confluent_service_account.admin ]
}
