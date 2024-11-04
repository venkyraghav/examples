# Notes

Create new TF workspace

```shell
terraform workspace new useast2
terraform workspace new useast1
```

Create VPC in `useast2`

```shell
terraform workspace select useast2
terraform apply -var-file=terraform-useast2.tfvars
```

Create VPC in `useast1`

```shell
terraform workspace select useast1
terraform apply -var-file=terraform-useast1.tfvars
```