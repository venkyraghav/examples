# Notes

## Instructions

To create the supported resources in supported aws regions run `./create`

To destroy the supported resources in supported aws regions run `./destroy`

## Supported resources

* VPC
* TGW
* CCKafka
* CCFlink
* EC2 (WIP)

## Supported regions

* useast1
* useast2

## General Command Usage

* Configure AWS credentials
* Configure Confluent API Key/Secrets in environment variable

```shell
export TF_VAR_confluent_cloud_api_key=<CC-APIKEY>
export TF_VAR_confluent_cloud_api_secret=<CC-SECRET>
```

* Create workspaces if not exists

```shell
cd $respectivedirectory

terraform init

terraform workspace list

terraform workspace new useast2
terraform workspace new useast1
```

* Modify `terraform-useast?.tfvars` file as appropriate
* Create resources in `useast2`

```shell
terraform workspace select useast2
terraform apply -var-file=terraform-useast2.tfvars
```

* Create resources in `useast1`

```shell
terraform workspace select useast1
terraform apply -var-file=terraform-useast1.tfvars
```
