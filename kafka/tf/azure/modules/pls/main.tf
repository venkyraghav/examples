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

data "confluent_network" "pl" {
  id = var.cc_network_pl_id

  environment {
    id = var.cc_env
  }
}

data "azurerm_resource_group" "rg" {
  name = var.rg
}

data "azurerm_virtual_network" "vnet" {
  name                = var.vnet
  resource_group_name = data.azurerm_resource_group.rg.name

  depends_on = [data.azurerm_resource_group.rg]
}

data "azurerm_subnet" "subnet" {
  for_each = var.subnet_zones

  name                 = each.value
  virtual_network_name = data.azurerm_virtual_network.vnet.name
  resource_group_name  = data.azurerm_resource_group.rg.name

  depends_on = [data.azurerm_virtual_network.vnet, data.azurerm_resource_group.rg]
}

resource "azurerm_private_dns_zone" "hz" {
  resource_group_name = data.azurerm_resource_group.rg.name
  name                = var.dns_domain

  tags = var.tags

  depends_on = [data.azurerm_resource_group.rg]
}

resource "azurerm_private_endpoint" "endpoint" {
  for_each = var.subnet_zones

  name                = "confluent-${var.cc_network_pl_id}-${each.key}"
  location            = var.region
  resource_group_name = data.azurerm_resource_group.rg.name

  subnet_id = data.azurerm_subnet.subnet[each.key].id

  private_service_connection {
    name                              = "confluent-${var.cc_network_pl_id}-${each.key}"
    is_manual_connection              = true
    private_connection_resource_alias = lookup(data.confluent_network.pl.azure[0].private_link_service_aliases, each.key, "\n\nerror: ${each.key} subnet is missing from CCN's Private Link service aliases")
    request_message                   = "PL"
  }

  tags       = var.tags
  depends_on = [data.confluent_network.pl, data.azurerm_resource_group.rg]
}

resource "azurerm_private_dns_zone_virtual_network_link" "hz" {
  name                  = data.azurerm_virtual_network.vnet.name
  resource_group_name   = data.azurerm_resource_group.rg.name
  private_dns_zone_name = azurerm_private_dns_zone.hz.name
  virtual_network_id    = data.azurerm_virtual_network.vnet.id

  tags = var.tags

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

  tags = var.tags

  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint
  ]
}

resource "azurerm_private_dns_a_record" "zonal" {
  for_each = var.subnet_zones

  name                = "*.az${each.key}"
  zone_name           = azurerm_private_dns_zone.hz.name
  resource_group_name = data.azurerm_resource_group.rg.name
  ttl                 = 60
  records = [
    azurerm_private_endpoint.endpoint[each.key].private_service_connection[0].private_ip_address,
  ]

  tags = var.tags

  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint
  ]
}
