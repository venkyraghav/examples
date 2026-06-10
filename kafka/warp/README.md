Create k8 cluster - gcp

Following https://docs.warpstream.com/warpstream/agent-setup/deploy
  https://github.com/warpstreamlabs/charts/tree/main/charts/warpstream-agent

Create `warpstream-values.yaml` file

```
helm repo add warpstream https://warpstreamlabs.github.io/charts
helm repo update

kubectl create namespace warp

helm upgrade --install warpstream-agent warpstream/warpstream-agent --namespace warp -f warpstream-values.yaml

```
