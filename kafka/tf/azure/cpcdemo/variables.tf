variable "aks_cluster_name" {
  description = "AKS Cluster Name"
  type        = string
}

variable "aks_rg" {
  description = "AKS Resource Group"
  type        = string
  default     = "venky_rg_eastus2"
}

variable "node_count" {
  description = "Node Count"
  type        = number
  default     = 1
}

variable "region" {
  description = "The AWS region"
  type        = string
  default     = "eastus2"
}

variable "instance_type" {
  description = "Type of instances"
  type        = string
  default     = "Standard_D4s_v4"
}
