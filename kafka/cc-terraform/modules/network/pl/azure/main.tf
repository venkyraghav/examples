terraform {
  required_version = ">= 0.14.0"
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.25.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.0.0"
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

data "confluent_schema_registry_cluster" "sr" {
  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_network" "private-link" {
  display_name     = var.confluent_cloud_network_name
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
  display_name = format("%s-access", var.confluent_cloud_network_name)
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

resource "confluent_kafka_cluster" "dedicated" {
  display_name = var.confluent_cloud_cluster_name
  availability = var.confluent_cloud_cluster_availability
  cloud        = confluent_network.private-link.cloud
  region       = confluent_network.private-link.region
  dedicated {
    cku = var.confluent_cloud_cluster_numckus
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

// 'app-manager-venky' service account is required in this configuration to create 'orders' topic and assign roles
// to 'app-producer-venky' and 'app-consumer-venky' service accounts.
resource "confluent_service_account" "app-manager-venky" {
  display_name = "app-manager-venky"
  description  = "Service account to manage 'inventory' Kafka cluster"
}

resource "confluent_role_binding" "app-manager-venky-kafka-cluster-admin" {
  principal   = "User:${confluent_service_account.app-manager-venky.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.dedicated.rbac_crn
  depends_on = [ 
    confluent_kafka_cluster.dedicated,
    confluent_network.private-link,
    confluent_private_link_access.azure
  ]
}

resource "confluent_api_key" "app-manager-venky-kafka-api-key" {
  display_name = "app-manager-venky-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-manager-venky' service account"

  # Set optional `disable_wait_for_ready` attribute (defaults to `false`) to `true` if the machine where Terraform is not run within a private network
  # disable_wait_for_ready = true

  owner {
    id          = confluent_service_account.app-manager-venky.id
    api_version = confluent_service_account.app-manager-venky.api_version
    kind        = confluent_service_account.app-manager-venky.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.dedicated.id
    api_version = confluent_kafka_cluster.dedicated.api_version
    kind        = confluent_kafka_cluster.dedicated.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }

  # The goal is to ensure that
  # 1. confluent_role_binding.app-manager-venky-kafka-cluster-admin is created before
  # confluent_api_key.app-manager-venky-kafka-api-key is used to create instances of
  # confluent_kafka_topic resource.
  # 2. Kafka connectivity through Azure PrivateLink is setup.
  depends_on = [
    confluent_role_binding.app-manager-venky-kafka-cluster-admin,
    confluent_kafka_cluster.dedicated,
    confluent_network.private-link,
    confluent_private_link_access.azure,
    azurerm_private_dns_zone_virtual_network_link.hz,
    azurerm_private_dns_a_record.rr,
    azurerm_private_dns_a_record.zonal
  ]
}

// Provisioning Kafka Topics requires access to the REST endpoint on the Kafka cluster
// If Terraform is not run from within the private network, this will not work
resource "confluent_kafka_topic" "orders" {
  kafka_cluster {
    id = confluent_kafka_cluster.dedicated.id
  }
  topic_name    = "orders"
  rest_endpoint = confluent_kafka_cluster.dedicated.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-venky-kafka-api-key.id
    secret = confluent_api_key.app-manager-venky-kafka-api-key.secret
  }
  depends_on = [
    confluent_role_binding.app-manager-venky-kafka-cluster-admin
  ]
}

resource "confluent_service_account" "app-consumer-venky" {
  display_name = "app-consumer-venky"
  description  = "Service account to consume from 'orders' topic of 'inventory' Kafka cluster"
}

resource "confluent_api_key" "app-consumer-venky-kafka-api-key" {
  display_name = "app-consumer-venky-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-consumer-venky' service account"

  # Set optional `disable_wait_for_ready` attribute (defaults to `false`) to `true` if the machine where Terraform is not run within a private network
  # disable_wait_for_ready = true

  owner {
    id          = confluent_service_account.app-consumer-venky.id
    api_version = confluent_service_account.app-consumer-venky.api_version
    kind        = confluent_service_account.app-consumer-venky.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.dedicated.id
    api_version = confluent_kafka_cluster.dedicated.api_version
    kind        = confluent_kafka_cluster.dedicated.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }

  depends_on = [
    confluent_private_link_access.azure,
    azurerm_private_dns_zone_virtual_network_link.hz,
    azurerm_private_dns_a_record.rr,
    azurerm_private_dns_a_record.zonal,
    confluent_role_binding.app-manager-venky-kafka-cluster-admin
  ]
}

resource "confluent_role_binding" "app-producer-venky-developer-write" {
  principal   = "User:${confluent_service_account.app-producer-venky.id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.dedicated.rbac_crn}/kafka=${confluent_kafka_cluster.dedicated.id}/topic=${confluent_kafka_topic.orders.topic_name}"
  depends_on = [
    confluent_service_account.app-producer-venky
  ]
}

resource "confluent_service_account" "app-producer-venky" {
  display_name = "app-producer-venky"
  description  = "Service account to produce to 'orders' topic of 'inventory' Kafka cluster"
}

resource "confluent_api_key" "app-producer-venky-kafka-api-key" {
  display_name = "app-producer-venky-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-producer-venky' service account"

  # Set optional `disable_wait_for_ready` attribute (defaults to `false`) to `true` if the machine where Terraform is not run within a private network
  # disable_wait_for_ready = true

  owner {
    id          = confluent_service_account.app-producer-venky.id
    api_version = confluent_service_account.app-producer-venky.api_version
    kind        = confluent_service_account.app-producer-venky.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.dedicated.id
    api_version = confluent_kafka_cluster.dedicated.api_version
    kind        = confluent_kafka_cluster.dedicated.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }

  depends_on = [
    confluent_private_link_access.azure,
    azurerm_private_dns_zone_virtual_network_link.hz,
    azurerm_private_dns_a_record.rr,
    azurerm_private_dns_a_record.zonal
  ]
}

// Note that in order to consume from a topic, the principal of the consumer ('app-consumer-venky' service account)
// needs to be authorized to perform 'READ' operation on both Topic and Group resources:
resource "confluent_role_binding" "app-consumer-venky-developer-read-from-topic" {
  principal   = "User:${confluent_service_account.app-consumer-venky.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${confluent_kafka_cluster.dedicated.rbac_crn}/kafka=${confluent_kafka_cluster.dedicated.id}/topic=${confluent_kafka_topic.orders.topic_name}"
  depends_on = [
    confluent_service_account.app-consumer-venky
  ]
}

resource "confluent_role_binding" "app-consumer-venky-developer-read-from-group" {
  principal = "User:${confluent_service_account.app-consumer-venky.id}"
  role_name = "DeveloperRead"
  // The existing value of crn_pattern's suffix (group=confluent_cli_consumer_*) are set up to match Confluent CLI's default consumer group ID ("confluent_cli_consumer_<uuid>").
  // https://docs.confluent.io/confluent-cli/current/command-reference/kafka/topic/confluent_kafka_topic_consume.html
  // Update it to match your target consumer group ID.
  crn_pattern = "${confluent_kafka_cluster.dedicated.rbac_crn}/kafka=${confluent_kafka_cluster.dedicated.id}/group=confluent_cli_consumer_*"
  depends_on = [
    confluent_service_account.app-consumer-venky
  ]
}

# https://docs.confluent.io/cloud/current/networking/private-links/azure-privatelink.html
# Set up Private Endpoints for Azure Private Link in your Azure subscription
# Set up DNS records to use Azure Private Endpoints
provider "azurerm" {
  subscription_id = var.arm_subscription_id
  client_id       = var.arm_client_id
  client_secret   = var.arm_client_secret
  tenant_id       = var.arm_tenant_id
  features {}
}

locals {
  dns_domain = confluent_network.private-link.dns_domain
  network_id = split(".", local.dns_domain)[0]
}

data "azurerm_resource_group" "rg" {
  name = var.resource_group
}

data "azurerm_virtual_network" "vnet" {
  name                = var.vnet_name
  resource_group_name = data.azurerm_resource_group.rg.name
}

data "azurerm_subnet" "subnet" {
  for_each = var.subnet_name_by_zone

  name                 = each.value
  virtual_network_name = data.azurerm_virtual_network.vnet.name
  resource_group_name  = data.azurerm_resource_group.rg.name
}

resource "azurerm_private_dns_zone" "hz" {
  resource_group_name = data.azurerm_resource_group.rg.name

  name = local.dns_domain
}

resource "azurerm_private_endpoint" "endpoint" {
  for_each = var.subnet_name_by_zone

  name                = "confluent-${local.network_id}-${each.key}"
  location            = var.region
  resource_group_name = data.azurerm_resource_group.rg.name

  subnet_id = data.azurerm_subnet.subnet[each.key].id

  private_service_connection {
    name                              = "confluent-${local.network_id}-${each.key}"
    is_manual_connection              = true
    private_connection_resource_alias = lookup(confluent_network.private-link.azure[0].private_link_service_aliases, each.key, "\n\nerror: ${each.key} subnet is missing from CCN's Private Link service aliases")
    request_message                   = "PL"
  }
  depends_on = [
    confluent_network.private-link
  ]
}

resource "azurerm_private_dns_zone_virtual_network_link" "hz" {
  name                  = data.azurerm_virtual_network.vnet.name
  resource_group_name   = data.azurerm_resource_group.rg.name
  private_dns_zone_name = azurerm_private_dns_zone.hz.name
  virtual_network_id    = data.azurerm_virtual_network.vnet.id
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
    for _, ep in flatten([values(azurerm_private_endpoint.endpoint),values(azurerm_private_endpoint.endpoint_vnet2)]) : ep.private_service_connection[0].private_ip_address
  ]
  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint,
    azurerm_private_endpoint.endpoint_vnet2
  ]
}

resource "azurerm_private_dns_a_record" "zonal" {
  for_each = var.subnet_name_by_zone

  name                = "*.az${each.key}"
  zone_name           = azurerm_private_dns_zone.hz.name
  resource_group_name = data.azurerm_resource_group.rg.name
  ttl                 = 60
  records = [
    azurerm_private_endpoint.endpoint[each.key].private_service_connection[0].private_ip_address,
    azurerm_private_endpoint.endpoint_vnet2[each.key].private_service_connection[0].private_ip_address,
  ]
  depends_on = [
    azurerm_private_dns_zone.hz,
    azurerm_private_endpoint.endpoint,
    azurerm_private_endpoint.endpoint_vnet2
  ]
}

# -------

data "azurerm_virtual_network" "vnet2" {
  name                = var.vnet2_name
  resource_group_name = data.azurerm_resource_group.rg.name
}

data "azurerm_subnet" "subnet_vnet2" {
  for_each = var.subnet_vnet2_name_by_zone

  name                 = each.value
  virtual_network_name = data.azurerm_virtual_network.vnet2.name
  resource_group_name  = data.azurerm_resource_group.rg.name
}

# No need now. Remove later
# resource "azurerm_private_dns_zone" "hz" {
#   resource_group_name = data.azurerm_resource_group.rg.name
# 
#   name = local.dns_domain
# }

resource "azurerm_private_endpoint" "endpoint_vnet2" {
  for_each = var.subnet_vnet2_name_by_zone

  name                = "confluent-${local.network_id}-vnet2-${each.key}"
  location            = var.region
  resource_group_name = data.azurerm_resource_group.rg.name

  subnet_id = data.azurerm_subnet.subnet_vnet2[each.key].id

  private_service_connection {
    name                              = "confluent-${local.network_id}-vnet2-${each.key}"
    is_manual_connection              = true
    private_connection_resource_alias = lookup(confluent_network.private-link.azure[0].private_link_service_aliases, each.key, "\n\nerror: ${each.key} subnet is missing from CCN's Private Link service aliases")
    request_message                   = "PL"
  }
  depends_on = [
    confluent_network.private-link
  ]
}

resource "azurerm_private_dns_zone_virtual_network_link" "hz2" {
  name                  = data.azurerm_virtual_network.vnet2.name
  resource_group_name   = data.azurerm_resource_group.rg.name
  private_dns_zone_name = azurerm_private_dns_zone.hz.name
  virtual_network_id    = data.azurerm_virtual_network.vnet2.id
  depends_on = [
    azurerm_private_dns_zone.hz
  ]
}

# No need now. Remove later
# resource "azurerm_private_dns_a_record" "rr" {
#   name                = "*"
#   zone_name           = azurerm_private_dns_zone.hz.name
#   resource_group_name = data.azurerm_resource_group.rg.name
#   ttl                 = 60
#   records = [
#     for _, ep in azurerm_private_endpoint.endpoint : ep.private_service_connection[0].private_ip_address
#   ]
#   depends_on = [
#     azurerm_private_dns_zone.hz,
#     azurerm_private_endpoint.endpoint
#   ]
# }
# 
# resource "azurerm_private_dns_a_record" "zonal" {
#   for_each = var.subnet_vnet2_name_by_zone
# 
#   name                = "*.az${each.key}"
#   zone_name           = azurerm_private_dns_zone.hz.name
#   resource_group_name = data.azurerm_resource_group.rg.name
#   ttl                 = 60
#   records = [
#     azurerm_private_endpoint.endpoint[each.key].private_service_connection[0].private_ip_address,
#   ]
#   depends_on = [
#     azurerm_private_dns_zone.hz,
#     azurerm_private_endpoint.endpoint
#   ]
# }