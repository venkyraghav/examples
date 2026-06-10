variable "region" {
  description = "The region of the vpc"
  type        = string
}

variable "arm_subscription_id" {
  description = "Azure Subscription ID"
  type        = string
}

variable "cc_env" {
  description = "Confluent cloud environment id"
  type        = string
}

variable "rg" {
  description = "Azure Resource group"
  type        = string
}

variable "vnet" {
  description = "Azure vnet name"
  type        = string
}

variable "dns_domain" {
  description = "Azure dns domain"
  type        = string
}

variable "cc_network_pl_id" {
  description = "Confluent network pl id"
  type        = string
}

variable "subnet_zones" {
  description = "Confluent subnet zones"
  type        = map(string)
}

variable "tags" {
  description = "Azure tags"
  type        = map(string)
}
