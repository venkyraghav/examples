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

data "confluent_environment" "env" {
  id = var.cc_env
}

data "confluent_network" "pl" {
  id = var.cc_network_id

  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_kafka_cluster" "dedicated" {
  display_name = var.cc_cluster_name
  availability = var.cc_cluster_availability
  cloud        = data.confluent_network.pl.cloud
  region       = data.confluent_network.pl.region

  dedicated {
    cku = var.cc_cluster_numckus
  }
  environment {
    id = data.confluent_environment.env.id
  }
  network {
    id = data.confluent_network.pl.id
  }
}
