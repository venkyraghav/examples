terraform {
  required_version = ">= 0.14.0"
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.3.0"
    }
  }
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

data "confluent_environment" "env" {
  id = var.confluent_cloud_env
}

data "confluent_network" "network" {
  id = var.confluent_cloud_network

  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_kafka_cluster" "dedicated" {
  display_name = var.confluent_cloud_cluster_name
  availability = var.confluent_cloud_cluster_availability
  cloud        = var.cloud_provider
  region       = var.region

  dedicated {
    cku = var.confluent_cloud_cluster_numckus
  }

  environment {
    id = data.confluent_environment.env.id
  }

  network {
    id = data.confluent_network.network.id
  }

  lifecycle {
    prevent_destroy = true
  }
}