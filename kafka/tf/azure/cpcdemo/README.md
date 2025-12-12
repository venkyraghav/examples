# Confluent Private Cloud Gateway Demo

## Steps to Deploy

1. Create 1 AKS clusters to CC gateway
2. Deploy CP operator
3. Deploy the gateway in the gateway AKS cluster
4. Create 1 AKS cluster for client
5. Run multiple clients from the client AKS cluster
6. Observe the gateway logs and metrics

## Create 1 AKS clusters to CC gateway
Execute the following command to create an AKS cluster:

```bash
terraform init
terraform plan
terraform apply
```

Follow the instructions in Azure AKS portal to connect to and check the AKS cluster.

```
az login
az account set --subscription <subscription-id>
az aks get-credentials --resource-group <resource-group-name> --name <aks-cluster-name>
kubectl get nodes
kubectl get deployments -A
kubectl get pods -A
```

## Deploy CP operator



## Deploy the gateway in the gateway AKS cluster

## Create 1 AKS cluster for client

## Run multiple clients from the client AKS cluster

## Observe the gateway logs and metrics
