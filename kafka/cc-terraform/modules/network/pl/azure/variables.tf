variable "confluent_cloud_api_key" {
  description = "Confluent Cloud API Key (also referred as Cloud API ID)."
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret."
  type        = string
  sensitive   = true
}

variable "region" {
  description = "The region of the vpc"
  type        = string
}

variable "arm_client_id" {
  description = "Azure client id"
  type = string
}

variable "arm_client_secret" {
  description = "Azure client secret"
  type = string
}

variable "arm_tenant_id" {
  description = "Azure Tenant id"
  type = string
}

variable "resource_group" {
  description = "Azure Resource group"
  type = string
}

variable "vnet_name" {
  description = "Azure vnet name"
  type = string
}

variable "subnet_name_by_zone" {
  description = "Azure vnet subnet zones"
  type = map(string)
}

variable "vnet2_name" {
  description = "Azure vnet2 name"
  type = string
}

variable "subnet_vnet2_name_by_zone" {
  description = "Azure vnet2 subnet zones"
  type = map(string)
}

variable "arm_subscription_id" {
  description = "Azure Subscription ID"
  type        = string
}

variable "confluent_cloud_cluster_name" {
  description = "Name of the Confluent Cloud cluster."
  type        = string
}

variable "confluent_cloud_env" {
  description = "Confluent cloud environment id"
  type        = string
}

variable "confluent_cloud_network_name" {
  description = "Confluent cloud network"
  type        = string
}

variable "confluent_cloud_cluster_availability" {
  description = "Confluent cloud Cluster availability"
  type        = string
  default = "SINGLE_ZONE"
}

variable "confluent_cloud_cluster_numckus" {
  description = "Confluent cloud num ckus"
  type        = string
  default = "1"
}

variable "tag_owner_email" {
  description = "Owner email id - responsible person"
  type        = string
}

variable "tag_purpose" {
  description = "Whats the purpose of this resource"
  type        = string
}

variable "tag_env" {
  description = "Lifecycle environment"
  type        = string
  default     = "dev"
}