output "out_app-manager-flink-api-key" {
  value       = format("%s:%s", confluent_api_key.app-manager-flink-api-key.id, confluent_api_key.app-manager-flink-api-key.secret)
  description = "TEST SA APIKey"
  sensitive = true
}
