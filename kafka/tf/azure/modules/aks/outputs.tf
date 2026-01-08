# Output kubeconfig
output "kube_config" {
  value     = azurerm_kubernetes_cluster.aks_cluster.kube_config_raw
  sensitive = true
}

# Output cluster name
output "cluster_name" {
  value = azurerm_kubernetes_cluster.aks_cluster.name
}

# Output cluster FQDN
output "cluster_fqdn" {
  value = azurerm_kubernetes_cluster.aks_cluster.fqdn
}

# Output cluster ID
output "cluster_id" {
  value = azurerm_kubernetes_cluster.aks_cluster.id
}
