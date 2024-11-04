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
