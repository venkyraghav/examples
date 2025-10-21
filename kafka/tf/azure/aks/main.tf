terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0" # use latest stable
    }
  }
}

locals {
  default_tags = {
    tags = {
      cflt_managed_id  = "vnarayanan"
      cflt_managed_by  = "user"
      cflt_environment = "dev"
      cflt_service     = "CPCGatewayDemo"
      cflt_keep_until  = "2025-12-31"
      owner_email      = "vnarayanan@confluent.io"
    }
  }
}
provider "azurerm" {
  features {}
}

data "azurerm_resource_group" "aks_rg" {
  name = var.aks_rg
}

# AKS Cluster
resource "azurerm_kubernetes_cluster" "aks_cluster" {
  name                = var.aks_cluster_name
  location            = data.azurerm_resource_group.aks_rg.location
  resource_group_name = data.azurerm_resource_group.aks_rg.name
  dns_prefix          = var.aks_cluster_name

  default_node_pool {
    name       = "systempool"
    node_count = var.node_count
    vm_size    = var.instance_type
    tags       = local.default_tags.tags
  }

  identity {
    type = "SystemAssigned"
  }

  tags = local.default_tags.tags
}

# Output kubeconfig
output "kube_config" {
  value     = azurerm_kubernetes_cluster.aks_cluster.kube_config_raw
  sensitive = true
}

# Output cluster name
output "cluster_name" {
  value = azurerm_kubernetes_cluster.aks_cluster.name
}

# Output cluster FQDN
output "cluster_fqdn" {
  value = azurerm_kubernetes_cluster.aks_cluster.fqdn
}

# Output cluster ID
output "cluster_id" {
  value = azurerm_kubernetes_cluster.aks_cluster.id
}
