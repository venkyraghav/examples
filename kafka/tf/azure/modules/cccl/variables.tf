variable "region" {
  description = "The region of the vpc"
  type        = string
  default     = "eastus2"
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

variable "cc_cl_name" {
  description = "Confluent cloud cluster link name"
  type        = string
  default     = "venky_test_cl"
}

variable "confluent_cloud_api_key" {
  description = "Confluent cloud API key"
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent cloud API secret"
  type        = string
}
