terraform {
  required_version = ">= 0.14.0"
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

locals {
  tags = {
    cflt_managed_id  = "vnarayanan"
    cflt_managed_by  = "user"
    cflt_environment = "dev"
    cflt_service     = "CPCGatewayDemo"
    cflt_keep_until  = "2026-12-31"
    owner_email      = "vnarayanan@confluent.io"
  }
}

provider "azurerm" {
  subscription_id = var.arm_subscription_id
  features {}
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}
