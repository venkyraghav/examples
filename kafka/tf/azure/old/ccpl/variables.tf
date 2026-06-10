variable "cluster_name" {
  description = "Cluster Name"
  type        = string
}

variable "region" {
  description = "The Azure region"
  type        = string
  default     = "eastus2"
}

variable "arm_client_id" {
  description = "Azure client id"
  type        = string
}

variable "arm_client_secret" {
  description = "Azure client secret"
  type        = string
}

variable "arm_tenant_id" {
  description = "Azure Tenant id"
  type        = string
}

variable "resource_group" {
  description = "Azure Resource group"
  type        = string
}

variable "vnet_name" {
  description = "Azure vnet name"
  type        = string
}

variable "subnet_name_by_zone" {
  description = "Azure vnet subnet zones"
  type        = map(string)
}

variable "vnet2_name" {
  description = "Azure vnet2 name"
  type        = string
}

variable "subnet_vnet2_name_by_zone" {
  description = "Azure vnet2 subnet zones"
  type        = map(string)
}

variable "arm_subscription_id" {
  description = "Azure Subscription ID"
  type        = string
}
