variable "confluent_cloud_api_key" {
  description = "Confluent Cloud API Key (also referred as Cloud API ID)."
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret."
  type        = string
  sensitive   = true
}

variable "cloud_provider" {
  description = "Cloud Provider"
  type        = string
}

variable "region" {
  description = "The region of the vpc"
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

variable "confluent_cloud_network" {
  description = "Confluent cloud network"
  type        = string
}

variable "confluent_cloud_cluster_availability" {
  description = "Confluent cloud Cluster availability"
  type        = string
}

variable "confluent_cloud_cluster_numckus" {
  description = "Confluent cloud num ckus"
  type        = string
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