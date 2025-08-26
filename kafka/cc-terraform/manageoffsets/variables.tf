variable "confluent_cloud_api_key" {
  description = "Confluent Cloud API Key (also referred as Cloud API ID)."
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret."
  type        = string
  sensitive   = true
}

variable "region" {
  description = "The region of the vpc"
  type        = string
}

variable "topic_name" {
  description = "Name of the topic."
  type        = string
}

variable "connector_name" {
  description = "Name of the connector."
  type        = string
}

variable "mongo_password" {
  description = "mongoDB Password."
  type        = string
}

variable "db_user" {
  description = "DB UserID."
  type        = string
}

variable "db_password" {
  description = "DB Password."
  type        = string
}

variable "db_host" {
  description = "DB Host."
  type        = string
}

variable "db_port" {
  description = "DB Port."
  type        = string
}

variable "confluent_cloud_cluster" {
  description = "Confluent Cluster ID."
  type        = string
}


variable "cc_venky_cluster_api_key" {
  description = "Confluent Cluster scope API Key."
  type        = string
}

variable "cc_venky_cluster_api_secret" {
  description = "Confluent Cluster scope API Secret."
  type        = string
}

variable "confluent_cloud_env" {
  description = "Confluent cloud environment id"
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