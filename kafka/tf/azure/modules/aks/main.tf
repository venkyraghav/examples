terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=4.57.0" # use latest stable
    }
  }
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
    tags       = var.tags
  }

  identity {
    type = "SystemAssigned"
  }

  tags = var.tags
}
