module "aks_cluster1" {
  source           = "../aks"
  aks_cluster_name = join("-", [var.aks_cluster_name, "1"])
  aks_rg           = var.aks_rg
  node_count       = var.node_count
  region           = var.region
  instance_type    = var.instance_type
}

# module "aks_cluster2" {
#   source           = "../aks"
#   aks_cluster_name = join("-", [var.aks_cluster_name, "2"])
#   aks_rg           = var.aks_rg
#   node_count       = var.node_count
#   region           = var.region
#   instance_type    = var.instance_type
# }

# module "aks_cluster3" {
#   source           = "../aks"
#   aks_cluster_name = join("-", [var.aks_cluster_name, "3"])
#   aks_rg           = var.aks_rg
#   node_count       = var.node_count
#   region           = var.region
#   instance_type    = var.instance_type
# }

output "cluster1_kube_config" {
  value     = module.aks_cluster1.kube_config
  sensitive = true
}

# Output cluster name
output "cluster1_cluster_name" {
  value = module.aks_cluster1.cluster_name
}

# Output cluster FQDN
output "cluster1_cluster_fqdn" {
  value = module.aks_cluster1.cluster_fqdn
}

# Output cluster ID
output "cluster1_cluster_id" {
  value = module.aks_cluster1.cluster_id
}

# # Output cluster kube_config
# output "cluster2_kube_config" {
#   value     = module.aks_cluster2.kube_config
#   sensitive = true
# }

# # Output cluster name
# output "cluster2_cluster_name" {
#   value = module.aks_cluster2.cluster_name
# }

# # Output cluster FQDN
# output "cluster2_cluster_fqdn" {
#   value = module.aks_cluster2.cluster_fqdn
# }

# # Output cluster ID
# output "cluster3_cluster_id" {
#   value = module.aks_cluster3.cluster_id
# }

# # Output cluster kube_config
# output "cluster3_kube_config" {
#   value     = module.aks_cluster3.kube_config
#   sensitive = true
# }

# # Output cluster name
# output "cluster3_cluster_name" {
#   value = module.aks_cluster3.cluster_name
# }

# # Output cluster FQDN
# output "cluster3_cluster_fqdn" {
#   value = module.aks_cluster3.cluster_fqdn
# }
