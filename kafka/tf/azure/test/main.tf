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
  description = "Confluent Cloud API Key (also referred as Cloud API ID)."
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret."
  type        = string
  sensitive   = true
}

variable "arm_subscription_id" {
  description = "Azure Subscription ID"
  type        = string
}

provider "azurerm" {
  subscription_id = var.arm_subscription_id
  features {}
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

data "confluent_network" "pl" {
  id = "n-pl2yo4"

  environment {
    id = "env-mzn3gw"
  }
}

data "azurerm_resource_group" "rg" {
  name = "MC_venky_rg_eastus2_venky-aks-eastus2_eastus2"
}

data "azurerm_virtual_network" "vnet" {
  name                = "aks-vnet-22750017"
  resource_group_name = data.azurerm_resource_group.rg.name
}

locals {
  subnets    = { "1" : "aks-subnet", "2" : "aks-subnet", "3" : "aks-subnet" }
  dns_domain = "dom4glleo0g.eastus2.azure.confluent.cloud"

  tags = {
    cflt_managed_id  = "vnarayanan"
    cflt_managed_by  = "user"
    cflt_environment = "dev"
    cflt_service     = "CPCGatewayDemo"
    cflt_keep_until  = "2026-12-31"
    owner_email      = "vnarayanan@confluent.io"
  }
}

data "azurerm_subnet" "subnet" {
  # for_each = var.subnet_name_by_zone
  for_each = local.subnets

  name = each.value
  # name                 = format("%s%s", "aks-subnet-", each.value)
  virtual_network_name = data.azurerm_virtual_network.vnet.name
  resource_group_name  = data.azurerm_resource_group.rg.name
}

resource "azurerm_private_dns_zone" "hz" {
  resource_group_name = data.azurerm_resource_group.rg.name
  name                = local.dns_domain

  tags = local.tags
}

resource "azurerm_private_endpoint" "endpoint" {
  # for_each = var.subnet_name_by_zone
  for_each = local.subnets

  name                = "confluent-plnet-${each.value}-${each.key}"
  location            = "eastus2"
  resource_group_name = data.azurerm_resource_group.rg.name

  subnet_id = data.azurerm_subnet.subnet[each.key].id

  private_service_connection {
    name                              = "confluent-plnet-${each.value}-${each.key}"
    is_manual_connection              = true
    private_connection_resource_alias = lookup(data.confluent_network.pl.azure[0].private_link_service_aliases, each.key, "\n\nerror: ${each.value} subnet is missing from CCN's Private Link service aliases")
    request_message                   = "PL"
  }

  tags = local.tags
}

resource "azurerm_private_dns_zone_virtual_network_link" "hz" {
  name                  = data.azurerm_virtual_network.vnet.name
  resource_group_name   = data.azurerm_resource_group.rg.name
  private_dns_zone_name = azurerm_private_dns_zone.hz.name
  virtual_network_id    = data.azurerm_virtual_network.vnet.id

  tags = local.tags

  depends_on = [
    azurerm_private_dns_zone.hz
  ]
}

resource "azurerm_private_dns_a_record" "rr" {
  name                = "*"
  zone_name           = azurerm_private_dns_zone.hz.name
  resource_group_name = data.azurerm_resource_group.rg.name
  ttl                 = 60
  records = [
    for _, ep in flatten([values(azurerm_private_endpoint.endpoint)]) : ep.private_service_connection[0].private_ip_address
  ]

  tags = local.tags

  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint
  ]
}

resource "azurerm_private_dns_a_record" "zonal" {
  for_each = local.subnets

  name                = "*.az${each.value}"
  zone_name           = azurerm_private_dns_zone.hz.name
  resource_group_name = data.azurerm_resource_group.rg.name
  ttl                 = 60
  records = [
    azurerm_private_endpoint.endpoint[each.key].private_service_connection[0].private_ip_address,
  ]

  tags = local.tags

  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint
  ]
}
