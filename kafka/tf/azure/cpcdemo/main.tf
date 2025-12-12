module "cpc_cluster_1" {
  source           = "../aks"
  aks_cluster_name = join("-", [var.aks_cluster_name, "1"])
  aks_rg           = var.aks_rg
  node_count       = var.node_count
  region           = var.region
  instance_type    = var.instance_type
  user_data        = templatefile("setup-ccgateway.tpl", {})
}

output "cluster1_kube_config" {
  value     = module.cpc_cluster_1.kube_config
  sensitive = true
}

# Output cluster name
output "cluster1_cluster_name" {
  value = module.cpc_cluster_1.cluster_name
}

# Output cluster FQDN
output "cluster1_cluster_fqdn" {
  value = module.cpc_cluster_1.cluster_fqdn
}

# Output cluster ID
output "cluster1_cluster_id" {
  value = module.cpc_cluster_1.cluster_id
}
