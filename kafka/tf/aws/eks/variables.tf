variable "vpc_id" {
  description = "The ID of the VPC"
  type        = string
}

variable "instance_count" {
  description = "# of instances"
  type        = number
  default     = 1
}

variable "aws_region" {
  description = "The AWS region"
  type        = string
  default     = "us-east-2"
}

variable "instance_type" {
  description = "Type of instances"
  type        = string
  default     = "t3.large"
}

variable "instance_ami" {
  description = "AMI of instances"
  type        = string
  default     = "ami-0cfde0ea8edd312d4"
}

variable "instance_keypair" {
  description = "Keypair to use in instances"
  type        = string
}

variable "aws_subnet_ids" {
  description = "AWS subnet Ids"
  type        = list(string)
}

variable "aws_security_group_ids" {
  description = "List of string of AWS Security Group Ids"
  type        = list(string)
}

variable "node_role_arn" {
  description = "Node role ARN"
  type        = string
}

variable "aws_iam_role_cluster_arn" {
  description = "Cluster role ARN"
  type        = string
}
