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

variable "cc_network_name" {
  description = "Confluent cloud network"
  type        = string
}
