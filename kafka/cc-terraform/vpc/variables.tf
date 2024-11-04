variable "region" {
  description = "The region of Confluent Cloud Network."
  type        = string
  default     = "us-east-1"
}

variable "vpc_name" {
  description = "Name of the VPC."
  type        = string
}

variable "cidr" {
  description = "The CIDR of Confluent Cloud Network."
  type        = string
}

variable "cidr_private_subnets" {
  description = "VPC Private subnet CIDRs"
  type        = list(string)
}

variable "cidr_public_subnets" {
  description = "VPC Public subnet CIDRs"
  type        = list(string)
}

variable "azs" {
  description = "Region AZs"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "aws_account_id" {
  description = "The AWS Account ID of the VPC owner (12 digits)."
  type        = string
}

variable "routes" {
  description = "The AWS VPC CIDR blocks or subsets. List of destination routes for traffic from Confluent VPC to your VPC via Transit Gateway."
  type        = list(string)
  default     = ["100.64.0.0/10", "10.0.0.0/8", "192.168.0.0/16", "172.16.0.0/12"]
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