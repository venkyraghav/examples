
variable "confluent_cloud_api_key" {
  type        = string
  description = "Confluent Cloud API Key"
}

variable "confluent_cloud_api_secret" {
  type        = string
  description = "Confluent Cloud API Secret"
  sensitive   = true
}

variable "idp_name" {
  description = "IDP Name"
  type        = string
  default     = "IDP"
}

variable "idp_description" {
  description = "IDP Description"
  type        = string
  default     = "IDP"
}

variable "oidc_discovery_url" {
  description = "OIDC Discovery URL"
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
