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

variable "cc_env" {
  description = "Confluent cloud environment id"
  type        = string
  default     = "env-mzn3gw"
}

variable "cc_network_name" {
  description = "Confluent cloud network"
  type        = string
  default     = "venky"
}

variable "cc_cluster_name" {
  description = "Confluent cloud cluster name"
  type        = string
  default     = "venky"
}

variable "aks_cluster_name" {
  description = "AKS Cluster Name"
  type        = string
  default     = "venky-aks"
}

variable "node_count" {
  description = "Node Count"
  type        = number
  default     = 2
}

variable "instance_type" {
  description = "Type of instances"
  type        = string
  default     = "Standard_D4s_v4"
}
