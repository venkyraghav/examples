# tags
tag_owner_email="vnarayanan@confluent.io"
tag_purpose = "LearnAWSTGWCCNetwork"
tag_env = "venkydev"

# Azure values
# The region of your VPC that you want to connect to Confluent Cloud Cluster
region = "eastus2"
# arm_client_id = "set arm_client_id in env"
# arm_client_secret = "set arm_client_secret in env"
# arm_tenant_id = "set arm_tenant_id in env"
# arm_subscription_id = "set arm_subscription_id in env"
resource_group = "venky_rg_eastus2"
vnet_name = "venky_rg_eastus2_vnet"
subnet_name_by_zone = { "1": "venky_rg_eastus2_subnet1", "2": "venky_rg_eastus2_subnet2", "3": "venky_rg_eastus2_subnet3" }

vnet2_name = "venky_rg_eastus2_vnet2"
subnet_vnet2_name_by_zone = { "1": "venky_rg_eastus2_vnet2_subnet1", "2": "venky_rg_eastus2_vnet2_subnet2", "3": "venky_rg_eastus2_vnet2_subnet3" }

# confluent information
confluent_cloud_cluster_name = "Venky Dedicated Cluster"
confluent_cloud_env = "env-mzn3gw"
confluent_cloud_network_name = "VenkyDedicatePL"
confluent_cloud_cluster_availability = "MULTI_ZONE"
confluent_cloud_cluster_numckus = 2
# confluent_cloud_api_key="set apikey in env"
# confluent_cloud_api_secret="set apisecret in env"
