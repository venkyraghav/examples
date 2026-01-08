output "cc_network_pl_id" {
  value       = confluent_network.pl.id
  description = "Confluent Network Private Link Id"
}

output "cc_network_pl_dns_domain" {
  value       = confluent_network.pl.dns_domain
  description = "Confluent Network Private Link DNS Domain"
}

output "cc_network_pl_network_id" {
  value       = split(".", confluent_network.pl.dns_domain)[0]
  description = "Confluent Network Private Link Network Id"
}

output "cc_network_pla_id" {
  value       = confluent_private_link_access.pla.id
  description = "Confluent Private Link Access Id"
}
