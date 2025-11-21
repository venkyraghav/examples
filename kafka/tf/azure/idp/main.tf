# Configure Azure IdP once in org level
# Datasource or import this in other modules
# data "confluent_identity_provider" "azure" {
#   id = "op-DAmQ"
# }
resource "confluent_identity_provider" "azure" {
  display_name = "Venky IDP: Azure AD"
  description  = "Venky IDP: Azure AD"
  issuer       = "https://login.microsoftonline.com/${var.tenant_id}/v2.0"
  jwks_uri     = "https://login.microsoftonline.com/common/discovery/v2.0/keys"
}

# Data source for Kafka cluster
# For Tier1 topics, source in both clusters
data "confluent_kafka_cluster" "cluster" {
  id = var.cluster_id
  environment {
    id = var.env_id
  }
}

# Configure Identity Pool idpool_dp2_write in the pipeline flow to create IDPools for DataProducts
resource "confluent_identity_pool" "idpool_dp2_write" {
  identity_provider {
    id = confluent_identity_provider.azure.id
  }
  display_name   = "idpool_dp2_write"
  description    = "Access to produce to Dataproduct2 topics"
  identity_claim = "claims.sub"
  filter         = "claims.iss == \"https://login.microsoftonline.com/${var.tenant_id}/v2.0\" && claims.aud == \"${var.api_scope}\" && \"idpool_dp2_write\" in claims.roles"
}

# Configure Identity Pool idpool_dp2_read in the pipeline flow to create IDPools for DataProducts
resource "confluent_identity_pool" "idpool_dp2_read" {
  identity_provider {
    id = confluent_identity_provider.azure.id
  }
  display_name   = "idpool_dp2_read"
  description    = "Access to consume from Dataproduct2 topics"
  identity_claim = "claims.sub"
  filter         = "claims.iss == \"https://login.microsoftonline.com/${var.tenant_id}/v2.0\" && claims.aud == \"${var.api_scope}\" && \"idpool_dp2_read\" in claims.roles"
}

# Provide DeveloperWrite on topic2 PREFIX to idpool_dp2_write in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_write" {
  principal = "User:${confluent_identity_pool.idpool_dp2_write.id}"
  role_name = "DeveloperWrite"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/topic=topic2*"
}

# Provide DeveloperRead on topic2 PREFIX to idpool_dp2_read in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_read" {
  principal = "User:${confluent_identity_pool.idpool_dp2_read.id}"
  role_name = "DeveloperRead"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/topic=topic2*"
}

# Provide DeveloperRead on all Consumer Groups to idpool_dp2_read in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_read_cg" {
  principal = "User:${confluent_identity_pool.idpool_dp2_read.id}"
  role_name = "DeveloperRead"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/group=*"
}
