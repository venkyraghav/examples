output "confluent_cluster_id" {
  value       = confluent_kafka_cluster.dedicated.id
  description = "Confluent Cluster Id"
}

output "confluent_cluster_bootstrap_endpoint" {
  value       = confluent_kafka_cluster.dedicated.bootstrap_endpoint
  description = "Confluent Cluster Bootstrap Endpoint"
}

output "confluent_cluster_rest_endpoint" {
  value       = confluent_kafka_cluster.dedicated.rest_endpoint
  description = "Confluent Cluster Rest Endpoint"
}

output "confluent_cluster_zones" {
  value       = confluent_kafka_cluster.dedicated.dedicated[0].zones
  description = "Confluent Network CIDR"
}

output "app-consumer-venky-kafka" {
  value       = format("%s:%s", confluent_api_key.app-consumer-venky-kafka-api-key.id, confluent_api_key.app-consumer-venky-kafka-api-key.secret)
  description = "app-consumer-venky-kafka APIKey"
  sensitive = true
}

output "app-manager-venky-kafka" {
  value       = format("%s:%s", confluent_api_key.app-manager-venky-kafka-api-key.id, confluent_api_key.app-manager-venky-kafka-api-key.secret)
  description = "app-manager-venky-kafka APIKey"
  sensitive = true
}

output "app-producer-venky-kafka" {
  value       = format("%s:%s", confluent_api_key.app-producer-venky-kafka-api-key.id, confluent_api_key.app-producer-venky-kafka-api-key.secret)
  description = "app-producer-venky-kafka APIKey"
  sensitive = true
}
