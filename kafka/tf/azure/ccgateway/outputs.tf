output "cc_network_pl_id_westus2" {
  value       = module.ccpl_westus2.cc_network_pl_id
  description = "Confluent Network Private Link Id"
}

output "cc_network_pl_dns_domain_westus2" {
  value       = module.ccpl_westus2.cc_network_pl_dns_domain
  description = "Confluent Network Private Link DNS Domain"
}

output "cc_network_pl_network_id_westus2" {
  value       = module.ccpl_westus2.cc_network_pl_network_id
  description = "Confluent Network Private Link Network Id"
}

output "cc_network_pl_id_eastus2" {
  value       = module.ccpl_eastus2.cc_network_pl_id
  description = "Confluent Network Private Link Id"
}

output "cc_network_pl_dns_domain_eastus2" {
  value       = module.ccpl_eastus2.cc_network_pl_dns_domain
  description = "Confluent Network Private Link DNS Domain"
}

output "cc_network_pl_network_id_eastus2" {
  value       = module.ccpl_eastus2.cc_network_pl_network_id
  description = "Confluent Network Private Link Network Id"
}

# westus2
output "confluent_cluster_id_westus2" {
  value       = module.cc_westus2.confluent_cluster_id
  description = "Confluent Cluster Id"
}

output "confluent_cluster_bootstrap_endpoint_westus2" {
  value       = module.cc_westus2.confluent_cluster_bootstrap_endpoint
  description = "Confluent Cluster Bootstrap Endpoint"
}

output "confluent_cluster_rest_endpoint_westus2" {
  value       = module.cc_westus2.confluent_cluster_rest_endpoint
  description = "Confluent Cluster Rest Endpoint"
}

output "confluent_cluster_zones_westus2" {
  value       = module.cc_westus2.confluent_cluster_zones
  description = "Confluent Network CIDR"
}

# eastus2
output "confluent_cluster_id_eastus2" {
  value       = module.cc_eastus2.confluent_cluster_id
  description = "Confluent Cluster Id"
}

output "confluent_cluster_bootstrap_endpoint_eastus2" {
  value       = module.cc_eastus2.confluent_cluster_bootstrap_endpoint
  description = "Confluent Cluster Bootstrap Endpoint"
}

output "confluent_cluster_rest_endpoint_eastus2" {
  value       = module.cc_eastus2.confluent_cluster_rest_endpoint
  description = "Confluent Cluster Rest Endpoint"
}

output "confluent_cluster_zones_eastus2" {
  value       = module.cc_eastus2.confluent_cluster_zones
  description = "Confluent Network CIDR"
}

# eastus2
output "aks_kube_config_eastus2" {
  value     = module.cc_gateway_eastus2.kube_config
  sensitive = true
}

# Output cluster name
output "aks_cluster_name_eastus2" {
  value = module.cc_gateway_eastus2.cluster_name
}

# Output cluster FQDN
output "aks_cluster_fqdn_eastus2" {
  value = module.cc_gateway_eastus2.cluster_fqdn
}

# Output cluster ID
output "aks_cluster_id_eastus2" {
  value = module.cc_gateway_eastus2.cluster_id
}

# westus2
output "aks_kube_config_westus2" {
  value     = module.cc_gateway_westus2.kube_config
  sensitive = true
}

# Output cluster name
output "aks_cluster_name_westus2" {
  value = module.cc_gateway_westus2.cluster_name
}

# Output cluster FQDN
output "aks_cluster_fqdn_westus2" {
  value = module.cc_gateway_westus2.cluster_fqdn
}

# Output cluster ID
output "aks_cluster_id_westus2" {
  value = module.cc_gateway_westus2.cluster_id
}
