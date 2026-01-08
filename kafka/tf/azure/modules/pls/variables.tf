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

variable "confluent_cluster_zones" {
  description = "Confluent cluster zones"
  type        = list(string)
}

variable "subnet_prefix" {
  description = "Azure subnet prefix"
  type        = string
}

variable "tags" {
  description = "Azure tags"
  type        = map(string)
}
