
variable "confluent_cloud_api_key" {
  type        = string
  description = "Confluent Cloud API Key"
}

variable "confluent_cloud_api_secret" {
  type        = string
  description = "Confluent Cloud API Secret"
  sensitive   = true
}

variable "tenant_id" {
  description = "Azure Tenant ID"
  type        = string
}

variable "api_scope" {
  description = "Azure EntraID API Scope"
  type        = string
}

variable "cluster_id" {
  description = "Confluent Cloud Cluster ID"
  type        = string
}

variable "env_id" {
  description = "Confluent Cloud Environment ID"
  type        = string
}
