
data "http" "oidc_discovery" {
  url = var.oidc_discovery_url

  request_headers = {
    Accept = "application/json"
  }
}

locals {
  response = jsondecode(data.http.oidc_discovery.response_body)
  issuer = local.response.issuer
  jwks_uri = local.response.jwks_uri
  token_endpoint = local.response.token_endpoint
}

# Configure IdP once in org level
# Datasource or import this in other modules
# data "confluent_identity_provider" "idp" {
#   id = "op-DAmQ"
# }
resource "confluent_identity_provider" "idp" {
  display_name = var.idp_name
  description  = var.idp_description
  issuer = local.issuer
  jwks_uri = local.jwks_uri

  depends_on = [data.http.oidc_discovery]
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
    id = confluent_identity_provider.idp.id
  }
  display_name   = "idpool_dp2_write"
  description    = "Access to produce to Dataproduct2 topics"
  identity_claim = "claims.sub"
  filter         = "claims.iss == \"${local.issuer}\" && claims.aud == \"${var.api_scope}\" && \"idpool_dp2_write\" in claims.roles"

  depends_on = [data.http.oidc_discovery, confluent_identity_provider.idp]
}

# Configure Identity Pool idpool_dp2_read in the pipeline flow to create IDPools for DataProducts
resource "confluent_identity_pool" "idpool_dp2_read" {
  identity_provider {
    id = confluent_identity_provider.idp.id
  }
  display_name   = "idpool_dp2_read"
  description    = "Access to consume from Dataproduct2 topics"
  identity_claim = "claims.sub"
  filter         = "claims.iss == \"${local.issuer}\" && claims.aud == \"${var.api_scope}\" && \"idpool_dp2_read\" in claims.roles"

  depends_on = [data.http.oidc_discovery, confluent_identity_provider.idp]
}

# Provide DeveloperWrite on topic2 PREFIX to idpool_dp2_write in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_write" {
  principal = "User:${confluent_identity_pool.idpool_dp2_write.id}"
  role_name = "DeveloperWrite"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/topic=topic2*"

  depends_on = [confluent_identity_pool.idpool_dp2_write, data.confluent_kafka_cluster.cluster]
}

# Provide DeveloperRead on topic2 PREFIX to idpool_dp2_read in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_read" {
  principal = "User:${confluent_identity_pool.idpool_dp2_read.id}"
  role_name = "DeveloperRead"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/topic=topic2*"

  depends_on = [confluent_identity_pool.idpool_dp2_read, data.confluent_kafka_cluster.cluster]
}

# Provide DeveloperRead on all Consumer Groups to idpool_dp2_read in the pipeline flow to create roles for DataProducts
resource "confluent_role_binding" "idpool_dp2_read_cg" {
  principal = "User:${confluent_identity_pool.idpool_dp2_read.id}"
  role_name = "DeveloperRead"

  crn_pattern = "${data.confluent_kafka_cluster.cluster.rbac_crn}/kafka=${data.confluent_kafka_cluster.cluster.id}/group=*"

  depends_on = [confluent_identity_pool.idpool_dp2_read, data.confluent_kafka_cluster.cluster]
}
