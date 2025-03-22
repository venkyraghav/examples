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

resource "confluent_connector" "sink" {
  environment {
    id = data.confluent_environment.env.id
  }
  
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }

  config_sensitive = {
    "kafka.auth.mode"            = "KAFKA_API_KEY"
    "kafka.api.key"              = var.cc_venky_cluster_api_key
    "kafka.api.secret"           = var.cc_venky_cluster_api_secret
    "connection.user"            = var.db_user
    "connection.password"        = var.db_password
    "connection.host"            = var.db_host
    "connection.port"            = var.db_port
    "ssl.mode"                   = "prefer"
  }

  config_nonsensitive = {
    "connector.class"            = "MySqlSink"
    "name"                       = var.connector_name
    "topics"                     = var.topic_name
    "input.data.format"         = "AVRO"
    "tasks.max"                  = "1"
    "db.name"                    = "test"
    "insert.mode"                = "INSERT"
    "auto.create"                = "true"
    "auto.evolve"                = "true"
  }
  
  // Total Message Count to Copy 108 + 57 + 37 = 202
  // Final Count = 3598 + 202 = 3800
  offsets { // Current Offset 608; Message To Copy 108
    partition = {
      "kafka_partition" = 0,
      "kafka_topic" = var.topic_name
    }
    offset = {
      "kafka_offset" = 500
    }
  }
  offsets { // Current Offset 557; Message To Copy 57
    partition = {
      "kafka_partition" = 1,
      "kafka_topic" = var.topic_name
    }
    offset = {
      "kafka_offset" = 500
    }
  }
  offsets { // Current Offset 537; Message To Copy 37
    partition = {
      "kafka_partition" = 5,
      "kafka_topic" = var.topic_name
    }
    offset = {
      "kafka_offset" = 500
    }
  }
}

resource "confluent_connector" "source" {
  environment {
    id = data.confluent_environment.env.id
  }
  
  kafka_cluster {
    id = data.confluent_kafka_cluster.test_cluster.id
  }

  // Block for custom *sensitive* configuration properties that are labelled with "Type: password" under "Configuration Properties" section in the docs:
  // https://docs.confluent.io/cloud/current/connectors/cc-mongo-db-source.html#configuration-properties
  config_sensitive = {
    "connection.password" = var.mongo_password
    "kafka.auth.mode"            = "KAFKA_API_KEY"
    "kafka.api.key"              = var.cc_venky_cluster_api_key
    "kafka.api.secret"           = var.cc_venky_cluster_api_secret
    "connection.host"          = "testcluster.wy6ey.mongodb.net"
    "connection.user"          = "vnarayanan"
  }

  // Block for custom *nonsensitive* configuration properties that are *not* labelled with "Type: password" under "Configuration Properties" section in the docs:
  // https://docs.confluent.io/cloud/current/connectors/cc-mongo-db-source.html#configuration-properties
  config_nonsensitive = {
    "connector.class"          = "MongoDbAtlasSource"
    "name"                     = "MongoDbAtlasSourceConnector_0"
    "topic.prefix"             = "tf"
    "database"                 = "sample_mflix"
    "collection"               = "movies"
    "poll.await.time.ms"       = "5000"
    "poll.max.batch.size"      = "1000"
    "copy.existing"            = "true"
    "output.data.format"       = "JSON"
    "tasks.max"                = "1"
    "startup.mode"             = "copy_existing"
  }

  offsets {
    partition = {
      "ns" = "mongodb+srv://testcluster.wy6ey.mongodb.net/sample_mflix.movies"
    }
    offset = {
      "_id" = "{\"_id\": {\"$oid\": \"573a1390f29313caabcd42e8\"}, \"copyingData\": true}"
      "copy" = "true"
    }
  }
}