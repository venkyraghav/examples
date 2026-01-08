variable "cc_env" {
  description = "Confluent cloud environment id"
  type        = string
}

variable "cc_network_id" {
  description = "Confluent cloud network id"
  type        = string
}

variable "cc_cluster_name" {
  description = "Confluent cloud cluster name"
  type        = string
}

variable "cc_cluster_numckus" {
  description = "Confluent cloud cluster number CKUs"
  type        = number
  default     = 1
}

variable "cc_cluster_availability" {
  description = "Confluent cloud cluster availability"
  type        = string
  default     = "SINGLE_ZONE"
}
