
resource "confluent_identity_provider" "azure" {
  display_name = "Venky IDP: Azure AD"
  description  = "Venky IDP: Azure AD"
  issuer       = "https://login.microsoftonline.com/${var.tenant_id}/v2.0"
  jwks_uri     = "https://login.microsoftonline.com/common/discovery/v2.0/keys"
}
