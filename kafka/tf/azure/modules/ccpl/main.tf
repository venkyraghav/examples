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

resource "confluent_network" "pl" {
  display_name     = var.cc_network_name
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

resource "confluent_private_link_access" "pla" {
  display_name = format("%s-access", var.cc_network_name)
  azure {
    subscription = var.arm_subscription_id
  }
  environment {
    id = data.confluent_environment.env.id
  }
  network {
    id = confluent_network.pl.id
  }
  depends_on = [
    confluent_network.pl
  ]
}
