output "oidc_issuer" {
  value = local.issuer
}

output "oidc_jwks_uri" {
  value = local.jwks_uri
}

output "oidc_token_endpoint" {
  value = local.token_endpoint
}

output "cc_idp_id" {
  value = confluent_identity_provider.idp.id
}

output "cc_idpool_dp2_write_id" {
  value = confluent_identity_pool.idpool_dp2_write.id
}

output "cc_idpool_dp2_read_id" {
  value = confluent_identity_pool.idpool_dp2_read.id
}
