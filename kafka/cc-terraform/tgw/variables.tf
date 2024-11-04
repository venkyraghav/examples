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

variable "confluent_cidr" {
  description = "The CIDR of Confluent Cloud Network."
  type        = string
}

variable "vpc_id" {
  description = "The AWS VPC ID of the VPC that you're connecting with Confluent Cloud."
  type        = string
}

variable "aws_vpc_cidr" {
  description = "The AWS VPC CIDR blocks or subsets. List of destination routes for traffic from Confluent VPC to your VPC via Transit Gateway."
  type        = list(string)
  default     = ["100.64.0.0/10", "10.0.0.0/8", "192.168.0.0/16", "172.16.0.0/12"]
}

variable "confluent_cloud_env" {
  description = "Confluent cloud environment id"
  type        = string
}

variable "confluent_cloud_network" {
  description = "Confluent cloud network id"
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