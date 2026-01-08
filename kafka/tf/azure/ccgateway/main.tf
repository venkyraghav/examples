# Create PL in eastus2
module "ccpl_eastus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source              = "../modules/ccpl"
  region              = "eastus2"
  arm_subscription_id = var.arm_subscription_id
  cc_env              = var.cc_env
  cc_network_name     = format("%s-eastus2", var.cc_network_name)
}

# Create PL in westus2
module "ccpl_westus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source              = "../modules/ccpl"
  region              = "westus2"
  arm_subscription_id = var.arm_subscription_id
  cc_env              = var.cc_env
  cc_network_name     = format("%s-westus2", var.cc_network_name)
}

# Create CC in eastus2
module "cc_eastus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/cc"
  cc_env                  = var.cc_env
  cc_network_id           = module.ccpl_eastus2.cc_network_pl_id
  cc_cluster_name         = format("%s-eastus2", var.cc_cluster_name)
  cc_cluster_numckus      = 2
  cc_cluster_availability = "MULTI_ZONE"

  depends_on = [module.ccpl_eastus2]
}

# Create CC in westus2
module "cc_westus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/cc"
  cc_env                  = var.cc_env
  cc_network_id           = module.ccpl_westus2.cc_network_pl_id
  cc_cluster_name         = format("%s-westus2", var.cc_cluster_name)
  cc_cluster_numckus      = 2
  cc_cluster_availability = "MULTI_ZONE"

  depends_on = [module.ccpl_westus2]
}

# Create Azure PL in eastus2
module "pls_eastus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/pls"
  region                  = "eastus2"
  rg                      = "venky_rg_eastus2"
  vnet                    = "venky_rg_eastus2_vnet"
  arm_subscription_id     = var.arm_subscription_id
  cc_env                  = var.cc_env
  confluent_cluster_zones = ["1", "2", "3"]
  subnet_prefix           = "venky_rg_eastus2_subnet"
  dns_domain              = module.ccpl_eastus2.cc_network_pl_dns_domain
  cc_network_pl_id        = module.ccpl_eastus2.cc_network_pl_id
  tags                    = local.tags

  depends_on = [module.ccpl_eastus2, module.cc_eastus2]
}

# Create Azure PL in westus2
module "pls_westus2" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/pls"
  region                  = "westus2"
  rg                      = "venky_rg_westus2"
  vnet                    = "venky_rg_westus2_vnet"
  arm_subscription_id     = var.arm_subscription_id
  cc_env                  = var.cc_env
  confluent_cluster_zones = ["1", "2", "3"]
  subnet_prefix           = "venky_rg_westus2_subnet"
  dns_domain              = module.ccpl_westus2.cc_network_pl_dns_domain
  cc_network_pl_id        = module.ccpl_westus2.cc_network_pl_id
  tags                    = local.tags

  depends_on = [module.ccpl_westus2, module.cc_westus2]
}

# Create Azure PL in cross connectivity from eastus2 to CC west cluster
module "pls_eastus2_xpost" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/pls"
  region                  = "eastus2"
  rg                      = "venky_rg_eastus2"
  vnet                    = "venky_rg_eastus2_vnet"
  arm_subscription_id     = var.arm_subscription_id
  cc_env                  = var.cc_env
  confluent_cluster_zones = ["1", "2", "3"]
  subnet_prefix           = "venky_rg_eastus2_subnet"
  dns_domain              = module.ccpl_westus2.cc_network_pl_dns_domain
  cc_network_pl_id        = module.ccpl_westus2.cc_network_pl_id
  tags                    = local.tags

  depends_on = [module.ccpl_westus2, module.cc_westus2]
}

# Create Azure PL in cross connectivity from westus2 to CC east cluster
module "pls_westus2_xpost" {
  providers = {
    azurerm   = azurerm
    confluent = confluent
  }
  source                  = "../modules/pls"
  region                  = "westus2"
  rg                      = "venky_rg_westus2"
  vnet                    = "venky_rg_westus2_vnet"
  arm_subscription_id     = var.arm_subscription_id
  cc_env                  = var.cc_env
  confluent_cluster_zones = ["1", "2", "3"]
  subnet_prefix           = "venky_rg_westus2_subnet"
  dns_domain              = module.ccpl_eastus2.cc_network_pl_dns_domain
  cc_network_pl_id        = module.ccpl_eastus2.cc_network_pl_id
  tags                    = local.tags

  depends_on = [module.ccpl_eastus2, module.cc_eastus2]
}

# Create AKS clusters in eastus2
module "cc_gateway_eastus2" {
  source           = "../modules/aks"
  aks_cluster_name = format("%s-eastus2", var.aks_cluster_name)
  aks_rg           = "venky_rg_eastus2"
  node_count       = var.node_count
  region           = "eastus2"
  instance_type    = var.instance_type
  tags = local.tags
  depends_on = [module.pls_eastus2_xpost]
}

# Create AKS clusters in westus2
module "cc_gateway_westus2" {
  source           = "../modules/aks"
  aks_cluster_name = format("%s-westus2", var.aks_cluster_name)
  aks_rg           = "venky_rg_westus2"
  node_count       = var.node_count
  region           = "westus2"
  instance_type    = var.instance_type
  tags = local.tags
  depends_on = [module.pls_westus2_xpost]
}

# Deploy CC Gateway using CFK in eastus2
# Deploy CC Gateway using CFK in westus2
