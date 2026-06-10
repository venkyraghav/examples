terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "=2.57.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=4.57.0"
    }
  }
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key    # optionally use CONFLUENT_CLOUD_API_KEY env var
  cloud_api_secret = var.confluent_cloud_api_secret # optionally use CONFLUENT_CLOUD_API_SECRET env var
}

# data "confluent_environment" "ce" {
#   id = var.cc_env
# }

resource "confluent_kafka_cluster" "kafka_clusters" {
  display_name = "venky-public"
  availability = "SINGLE_ZONE"
  cloud        = "AZURE"
  region       = "eastus2"

  dedicated {
    cku = 1
  }

  environment {
    id = var.cc_env
  }

}

resource "confluent_kafka_cluster" "cc_vnetpeercluster" {
  display_name = "venky-vnetpeercluster"
  availability = "SINGLE_ZONE"
  cloud        = "AZURE"
  region       = "eastus2"

  dedicated {
    cku = 1
  }

  environment {
    id = var.cc_env
  }

}

# datasource the admin service account
resource "confluent_service_account" "admin" {
  display_name = "venky-admin"
  description  = "venky-admin"
}

# Create APIKey for VNETPeerCluster for admin service account
resource "confluent_api_key" "admin_sa_vnetpeercluster_key" {
  display_name = format("%s-vnetpeercluster-key", confluent_service_account.admin.display_name)
  description  = format("Kafka APIKey owned by %s for vnetpeercluster", confluent_service_account.admin.display_name)
  owner {
    id          = confluent_service_account.admin.id
    api_version = confluent_service_account.admin.api_version
    kind        = confluent_service_account.admin.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.cc_vnetpeercluster.id
    api_version = confluent_kafka_cluster.cc_vnetpeercluster.api_version
    kind        = confluent_kafka_cluster.cc_vnetpeercluster.kind

    environment {
      id = var.cc_env
    }
  }
  depends_on = [
    confluent_service_account.admin,
    confluent_kafka_cluster.cc_vnetpeercluster
  ]
}

# Create APIKey for public clusters for admin service account
resource "confluent_api_key" "admin_sa_publiccluster_keys" {
  display_name = format("%s-public-%s-key", confluent_kafka_cluster.kafka_clusters.id, confluent_service_account.admin.display_name)
  description  = format("Kafka APIKey owned by %s for public cluster %s", confluent_kafka_cluster.kafka_clusters.id, confluent_service_account.admin.display_name)
  owner {
    id          = confluent_service_account.admin.id
    api_version = confluent_service_account.admin.api_version
    kind        = confluent_service_account.admin.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.kafka_clusters.id
    api_version = confluent_kafka_cluster.kafka_clusters.api_version
    kind        = confluent_kafka_cluster.kafka_clusters.kind

    environment {
      id = var.cc_env
    }
  }
  depends_on = [
    confluent_service_account.admin,
    confluent_kafka_cluster.kafka_clusters
  ]
}

# Create Role bindings for VNETPeerCluster for admin service account
resource "confluent_role_binding" "admin_sa_vnetpeercluster_rb" {
  principal   = "User:${confluent_service_account.admin.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.cc_vnetpeercluster.rbac_crn

  depends_on = [
    confluent_service_account.admin,
    confluent_kafka_cluster.cc_vnetpeercluster
  ]
}

# Create Role bindings for public clusters for admin service account
resource "confluent_role_binding" "admin_sa_publiccluster_rbs" {
  principal   = "User:${confluent_service_account.admin.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.kafka_clusters.rbac_crn

  depends_on = [
    confluent_service_account.admin,
    confluent_kafka_cluster.kafka_clusters
  ]
}

# resource "confluent_cluster_link" "vnetpeer2publicclusters" {
#   link_name       = "CL_${confluent_kafka_cluster.cc_vnetpeercluster.id}_to_${confluent_kafka_cluster.kafka_clusters.id}"
#   link_mode       = "SOURCE"
#   connection_mode = "OUTBOUND"

#   source_kafka_cluster {
#     id            = confluent_kafka_cluster.cc_vnetpeercluster.id
#     rest_endpoint = confluent_kafka_cluster.cc_vnetpeercluster.rest_endpoint
#     credentials {
#       key    = confluent_api_key.admin_sa_vnetpeercluster_key.id
#       secret = confluent_api_key.admin_sa_vnetpeercluster_key.secret
#     }
#   }

#   destination_kafka_cluster {
#     id                 = confluent_kafka_cluster.kafka_clusters.id
#     bootstrap_endpoint = confluent_kafka_cluster.kafka_clusters.bootstrap_endpoint
#     credentials {
#       key    = confluent_api_key.admin_sa_publiccluster_keys.id
#       secret = confluent_api_key.admin_sa_publiccluster_keys.secret
#     }
#   }

#   config = {
#     "auto.create.mirror.topics.enable" = false
#     "consumer.offset.sync.enable"      = true
#     "acl.sync.enable"                  = false
#     "cluster.link.prefix"              = ""
#   }

#   depends_on = [
#     confluent_kafka_cluster.cc_vnetpeercluster,
#     confluent_kafka_cluster.kafka_clusters,
#     confluent_api_key.admin_sa_vnetpeercluster_key,
#     confluent_api_key.admin_sa_publiccluster_keys,
#     confluent_cluster_link.vnetpeer2publicclusters-dest-inbound
#   ]
# }

# resource "confluent_cluster_link" "vnetpeer2publicclusters-dest-inbound" {
#   link_name       = "CL_${confluent_kafka_cluster.cc_vnetpeercluster.id}_to_${confluent_kafka_cluster.kafka_clusters.id}"
#   link_mode       = "DESTINATION"
#   connection_mode = "INBOUND"

#   source_kafka_cluster {
#     id                 = confluent_kafka_cluster.cc_vnetpeercluster.id
#     bootstrap_endpoint = confluent_kafka_cluster.cc_vnetpeercluster.bootstrap_endpoint
#   }

#   destination_kafka_cluster {
#     id            = confluent_kafka_cluster.kafka_clusters.id
#     rest_endpoint = confluent_kafka_cluster.kafka_clusters.rest_endpoint
#     credentials {
#       key    = confluent_api_key.admin_sa_publiccluster_keys.id
#       secret = confluent_api_key.admin_sa_publiccluster_keys.secret
#     }
#   }

#   config = {
#     "auto.create.mirror.topics.enable" = false
#     "consumer.offset.sync.enable"      = true
#     "acl.sync.enable"                  = false
#     "cluster.link.prefix"              = ""
#   }

#   depends_on = [
#     confluent_kafka_cluster.cc_vnetpeercluster,
#     confluent_kafka_cluster.kafka_clusters,
#     confluent_api_key.admin_sa_publiccluster_keys
#   ]
# }

resource "confluent_kafka_topic" "testtopic" {
  topic_name = "venky-test"
  kafka_cluster {
    id = confluent_kafka_cluster.cc_vnetpeercluster.id
  }
  rest_endpoint = confluent_kafka_cluster.cc_vnetpeercluster.rest_endpoint
  credentials {
    key    = confluent_api_key.admin_sa_vnetpeercluster_key.id
    secret = confluent_api_key.admin_sa_vnetpeercluster_key.secret
  }

  depends_on = [
    confluent_kafka_cluster.kafka_clusters,
    confluent_api_key.admin_sa_vnetpeercluster_key,
  ]
}

# resource "confluent_kafka_mirror_topic" "vnetpeer2publiccluster_eastus2" {
#   source_kafka_topic {
#     topic_name = "venky-test"
#   }
#   mirror_topic_name = "venky-test"
#   cluster_link {
#     link_name = confluent_cluster_link.vnetpeer2publicclusters.link_name
#   }
#   kafka_cluster {
#     id            = confluent_kafka_cluster.kafka_clusters.id
#     rest_endpoint = confluent_kafka_cluster.kafka_clusters.rest_endpoint
#     credentials {
#       key    = confluent_api_key.admin_sa_publiccluster_keys.id
#       secret = confluent_api_key.admin_sa_publiccluster_keys.secret
#     }
#   }

#   depends_on = [
#     confluent_kafka_cluster.kafka_clusters,
#     confluent_api_key.admin_sa_publiccluster_keys,
#     confluent_cluster_link.vnetpeer2publicclusters,
#     confluent_cluster_link.vnetpeer2publicclusters-dest-inbound,
#     confluent_kafka_topic.testtopic,
#   ]
# }
