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

data "confluent_network" "network" {
  id = var.confluent_cloud_network

  environment {
    id = data.confluent_environment.env.id
  }
}

data "confluent_kafka_cluster" "test_cluster" {
  id = var.confluent_cloud_cluster

  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_service_account" "cloud_admin" {
  display_name = format("cloud_admin_%s", var.tag_env)
  description  = format("Service account to manage the Kafka cluster for %s", var.tag_owner_email)
}

resource "confluent_role_binding" "cloud_admin_rb" {
  principal   = "User:${confluent_service_account.cloud_admin.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = data.confluent_kafka_cluster.test_cluster.rbac_crn
}

resource "confluent_api_key" "cloud_admin_kafka_apikey" {
  display_name = format("cloud_admin_kafka_apikey_%s", var.tag_env)
  description  = format("Kafka API Key that is owned by cloud admin service account %s", var.tag_owner_email)

  owner {
    id          = confluent_service_account.cloud_admin.id
    api_version = confluent_service_account.cloud_admin.api_version
    kind        = confluent_service_account.cloud_admin.kind
  }

  managed_resource {
    id          = data.confluent_kafka_cluster.test_cluster.id
    api_version = data.confluent_kafka_cluster.test_cluster.api_version
    kind        = data.confluent_kafka_cluster.test_cluster.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }

  depends_on = [
    confluent_role_binding.cloud_admin_rb
  ]
}

resource "confluent_kafka_topic" "test_topic" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  topic_name         = var.topic_name
  rest_endpoint      = data.confluent_kafka_cluster.test_cluster.rest_endpoint
  partitions_count    = 3

  credentials {
    key    = confluent_api_key.cloud_admin_kafka_apikey.id
    secret = confluent_api_key.cloud_admin_kafka_apikey.secret
  }
}

resource "confluent_service_account" "test_sa_app" {
  display_name = format("test_sa_app_%s", var.tag_env)
  description  = format("Service Account for test app for %s", var.tag_owner_email)
}

resource "confluent_api_key" "test_sa_kafka_api_key" {
  display_name = format("test_sa_kafka_api_key_%s", var.tag_env)
  description  = format("Kafka API Key that is owned by test_sa_app service account %s", var.tag_owner_email)

  owner {
    id          = confluent_service_account.test_sa_app.id
    api_version = confluent_service_account.test_sa_app.api_version
    kind        = confluent_service_account.test_sa_app.kind
  }

  managed_resource {
    id          = data.confluent_kafka_cluster.test_cluster.id
    api_version = data.confluent_kafka_cluster.test_cluster.api_version
    kind        = data.confluent_kafka_cluster.test_cluster.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }
}

resource "confluent_kafka_acl" "simple_producer" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.topic_name
  pattern_type  = "LITERAL"
  principal     = format("User:%s", confluent_service_account.test_sa_app.id)
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = data.confluent_kafka_cluster.test_cluster.rest_endpoint

  credentials {
    key    = confluent_api_key.cloud_admin_kafka_apikey.id
    secret = confluent_api_key.cloud_admin_kafka_apikey.secret
  }
}

resource "confluent_service_account" "test_sa_app_complex" {
  display_name = format("test_sa_app_complex_%s", var.tag_env)
  description  = format("Service Account for test app complex for %s", var.tag_owner_email)
}

resource "confluent_api_key" "test_sa_app_complex_kafka_api_key" {
  display_name = format("test_sa_app_complex_kafka_api_key for %s", var.tag_env)
  description  = format("Kafka API Key that is owned by test_sa_app_complex_kafka service account %s", var.tag_owner_email)

  owner {
    id          = confluent_service_account.test_sa_app_complex.id
    api_version = confluent_service_account.test_sa_app_complex.api_version
    kind        = confluent_service_account.test_sa_app_complex.kind
  }

  managed_resource {
    id          = data.confluent_kafka_cluster.test_cluster.id
    api_version = data.confluent_kafka_cluster.test_cluster.api_version
    kind        = data.confluent_kafka_cluster.test_cluster.kind

    environment {
      id = data.confluent_environment.env.id
    }
  }
}

resource "confluent_kafka_acl" "complex_producer" {
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.topic_name
  pattern_type  = "LITERAL"
  principal     = format("User:%s", confluent_service_account.test_sa_app_complex.id)
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = data.confluent_kafka_cluster.test_cluster.rest_endpoint

  credentials {
    key    = confluent_api_key.cloud_admin_kafka_apikey.id
    secret = confluent_api_key.cloud_admin_kafka_apikey.secret
  }
}