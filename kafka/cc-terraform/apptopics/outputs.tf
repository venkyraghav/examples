output "test_sa" {
  value       = format("%s:%s", confluent_api_key.test_sa_kafka_api_key.id, confluent_api_key.test_sa_kafka_api_key.secret)
  description = "TEST SA APIKey"
  sensitive = true
}

output "test_sa_complex" {
  value       = format("%s:%s", confluent_api_key.test_sa_app_complex_kafka_api_key.id, confluent_api_key.test_sa_app_complex_kafka_api_key.secret)
  description = "TEST SA Complex APIKey"
  sensitive = true
}

output "cloud_admin" {
  value       = format("%s:%s", confluent_api_key.cloud_admin_kafka_apikey.id, confluent_api_key.cloud_admin_kafka_apikey.secret)
  description = "Cloud Admin APIKey"
  sensitive = true
}
