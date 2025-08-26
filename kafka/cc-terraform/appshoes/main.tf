terraform {
  required_version = ">= 0.14.0"
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.3.0"
    }
  }
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

data "confluent_environment" "env" {
  id = var.confluent_cloud_env
}

data "confluent_kafka_cluster" "test_cluster" {
  id = var.confluent_cloud_cluster

  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_kafka_topic" "shoes" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  topic_name         = "shoes"
  rest_endpoint      = data.confluent_kafka_cluster.test_cluster.rest_endpoint
  partitions_count    = 6

  credentials {
    key    = var.cc_venky_cluster_api_key
    secret = var.cc_venky_cluster_api_secret
  }
}

resource "confluent_kafka_topic" "shoe_customers" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  topic_name         = "shoe_customers"
  rest_endpoint      = data.confluent_kafka_cluster.test_cluster.rest_endpoint
  partitions_count    = 6

  credentials {
    key    = var.cc_venky_cluster_api_key
    secret = var.cc_venky_cluster_api_secret
  }
}

resource "confluent_kafka_topic" "shoe_orders" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  topic_name         = "shoe_orders"
  rest_endpoint      = data.confluent_kafka_cluster.test_cluster.rest_endpoint
  partitions_count    = 6

  credentials {
    key    = var.cc_venky_cluster_api_key
    secret = var.cc_venky_cluster_api_secret
  }
}

resource "confluent_connector" "Datagen_Connector_Shoes" {
  environment {
    id = data.confluent_environment.env.id
  }
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }

  config_sensitive = {
    "kafka.api.key"     = var.cc_venky_cluster_api_key
    "kafka.api.secret"  = var.cc_venky_cluster_api_secret
  }

  config_nonsensitive = {
    "connector.class"          = "DatagenSource"
    "name"                     = "Datagen_Connector_Shoes"
    "kafka.auth.mode"          = "KAFKA_API_KEY"
    "kafka.topic"              = confluent_kafka_topic.shoes.topic_name
    "output.data.format"       = "AVRO"
    "quickstart"               = "shoes"
    "tasks.max"                = "1"
  }
}

resource "confluent_connector" "Datagen_Connector_ShoeOrders" {
  environment {
    id = data.confluent_environment.env.id
  }
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }

  config_sensitive = {
    "kafka.api.key"     = var.cc_venky_cluster_api_key
    "kafka.api.secret"  = var.cc_venky_cluster_api_secret
  }

  config_nonsensitive = {
    "connector.class"          = "DatagenSource"
    "name"                     = "Datagen_Connector_ShoeOrders"
    "kafka.auth.mode"          = "KAFKA_API_KEY"
    "kafka.topic"              = confluent_kafka_topic.shoe_orders.topic_name
    "output.data.format"       = "AVRO"
    "quickstart"               = "shoe_orders"
    "tasks.max"                = "1"
  }
}

resource "confluent_connector" "Datagen_Connector_ShoeCustomers" {
  environment {
    id = data.confluent_environment.env.id
  }
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }

  config_sensitive = {
    "kafka.api.key"     = var.cc_venky_cluster_api_key
    "kafka.api.secret"  = var.cc_venky_cluster_api_secret
  }

  config_nonsensitive = {
    "connector.class"          = "DatagenSource"
    "name"                     = "Datagen_Connector_ShoeCustomers"
    "kafka.auth.mode"          = "KAFKA_API_KEY"
    "kafka.topic"              = confluent_kafka_topic.shoe_customers.topic_name
    "output.data.format"       = "AVRO"
    "quickstart"               = "shoe_customers"
    "tasks.max"                = "1"
  }
}