terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.39.0"
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

variable "confluent_cloud_sr_api_key" {
  type        = string
  description = "Confluent Cloud SR API Key"
}

variable "confluent_cloud_sr_api_secret" {
  type        = string
  description = "Confluent Cloud SR API Secret"
  sensitive   = true
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key    # optionally use CONFLUENT_CLOUD_API_KEY env var
  cloud_api_secret = var.confluent_cloud_api_secret # optionally use CONFLUENT_CLOUD_API_SECRET env var
}

data "confluent_environment" "development" {
  display_name = "venky"
}

resource "confluent_kafka_cluster" "basic" {
  display_name = "basic_kafka_cluster"
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = "us-east-2"
  basic {}

  environment {
    id = data.confluent_environment.development.id
  }
}

resource "confluent_tag" "region" {
  name        = replace("us-east-2", "-", "_")
  description = "Region tag"

  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = var.confluent_cloud_sr_api_key
    secret = var.confluent_cloud_sr_api_secret
  }
}

resource "confluent_tag" "test" {
  name        = "Test"
  description = "Test tag"

  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = var.confluent_cloud_sr_api_key
    secret = var.confluent_cloud_sr_api_secret
  }
}

data "confluent_schema_registry_cluster" "essentials" {
  environment { id = data.confluent_environment.development.id }
}

resource "confluent_tag_binding" "test_cluster" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = var.confluent_cloud_sr_api_key
    secret = var.confluent_cloud_sr_api_secret
  }
  tag_name    = replace(confluent_tag.test.name, "-", "_")
  entity_name = confluent_kafka_cluster.basic.id
  entity_type = "kafka_logical_cluster"
  depends_on  = [confluent_kafka_cluster.basic, confluent_tag.test]
}

resource "confluent_tag_binding" "region_cluster" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = var.confluent_cloud_sr_api_key
    secret = var.confluent_cloud_sr_api_secret
  }
  tag_name    = replace(confluent_kafka_cluster.basic.region, "-", "_")
  entity_name = confluent_kafka_cluster.basic.id
  entity_type = "kafka_logical_cluster"
  depends_on  = [confluent_kafka_cluster.basic, confluent_tag.test]
}

resource "confluent_tag_binding" "test_env" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = var.confluent_cloud_sr_api_key
    secret = var.confluent_cloud_sr_api_secret
  }
  tag_name    = confluent_tag.test.name
  entity_name = data.confluent_environment.development.id
  entity_type = "cf_environment"
  depends_on  = [confluent_tag.test]
}
