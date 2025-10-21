# Notes

## What does it do

* Setup the following
  * confluent cloud cluster in private network
  * azure private endpoint
  * azure private DNS zone

## Dependencies

* Terraform
* azure login, ARM ids

## Steps

* Set the following variables in `vars` file or as `environment` variables

```properties
arm_client_id = "set arm_client_id in env"
arm_client_secret = "set arm_client_secret in env"
arm_tenant_id = "set arm_tenant_id in env"
arm_subscription_id = "set arm_subscription_id in env"
```

```shell
export TF_VAR_arm_client_id
export TF_VAR_arm_client_secret
export TF_VAR_arm_subscription_id
export TF_VAR_arm_tenant_id
export TF_VAR_confluent_cloud_api_key
export TF_VAR_confluent_cloud_api_secret
```

* Run

```shell
terraform init
terraform plan
terraform apply
terraform output -raw app-producer-venky-kafka
```
