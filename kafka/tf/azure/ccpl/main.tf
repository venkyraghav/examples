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

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key    # optionally use CONFLUENT_CLOUD_API_KEY env var
  cloud_api_secret = var.confluent_cloud_api_secret # optionally use CONFLUENT_CLOUD_API_SECRET env var
}

data "confluent_environment" "env" {
  display_name = "venky"
}

resource "confluent_kafka_cluster" "dedicated" {
  display_name = var.cluster_name
  availability = "MULTI_ZONE"
  cloud        = "AZURE"
  region       = var.region
  dedicated {
    cku = 2
  }
  environment {
    id = data.confluent_environment.env.id
  }
  network {
    id = confluent_network.private-link.id
  }
  depends_on = [
    confluent_network.private-link
  ]
}

resource "confluent_network" "private-link" {
  display_name     = format("%s-pl", var.cluster_name)
  cloud            = "AZURE"
  region           = var.region
  connection_types = ["PRIVATELINK"]
  environment {
    id = data.confluent_environment.env.id
  }
  dns_config {
    resolution = "PRIVATE"
  }
}

resource "confluent_private_link_access" "azure" {
  display_name = format("%s-access", confluent_network.private-link.display_name)
  azure {
    subscription = var.arm_subscription_id
  }
  environment {
    id = data.confluent_environment.env.id
  }
  network {
    id = confluent_network.private-link.id
  }
  depends_on = [
    confluent_network.private-link
  ]
}
