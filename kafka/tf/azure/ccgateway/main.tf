# Create AKS clusters in eastus2
module "cc_gateway_eastus2" {
  source           = "../modules/aks"
  aks_cluster_name = format("%s-eastus2", var.aks_cluster_name)
  aks_rg           = "venky_rg_eastus2"
  node_count       = var.node_count
  region           = "eastus2"
  instance_type    = var.instance_type
  tags             = local.tags
}
