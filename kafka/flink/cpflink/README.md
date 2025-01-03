# Flink Workshop

## CP Flink

### k8 Cluster Setup (GKE)

* `gcloud` needs less or equal to `python 3.10`

```shell
export CLOUDSDK_PYTHON=/opt/homebrew/bin/python3.10
```

* Create k8 cluster

```shell
gcloud container clusters create venky-cp-flink  --zone=us-east1-b --num-nodes=6 --machine-type=e2-standard-4 --project solutionsarchitect-01
```

### TLS Certs

* Created `ca-csr-json`, `ca-config.json`, and `server-domain.json` files

* IMPORTANT: namespace should match the SAN name part. For ex: Resources deployed in `confluent` namespace can be addressed using DNS wildcard `*.confluent.svc.cluster.local`

* Commands below

```shell
brew install cfssl

mkdir -p ./generated && cfssl gencert -initca ./ca-csr.json | cfssljson -bare ./generated/ca -

# Validate Certificate Authority
openssl x509 -in ./generated/ca.pem -text -noout

# Create server certificates with the appropriate SANs (SANs listed in server-domain.json)
cfssl gencert -ca=./generated/ca.pem \
-ca-key=./generated/ca-key.pem \
-config=./ca-config.json \
-profile=server ./server-domain.json | cfssljson -bare ./generated/server

# Validate server certificate and SANs
openssl x509 -in ./generated/server.pem -text -noout

openssl pkcs12 -export -in server.pem -inkey server-key.pem -out jks/server.p12 -name "Server"

keytool -importkeystore -srckeystore jks/server.p12 -srcstoretype pkcs12 -destkeystore jks/keystore.jks

keytool -import -v -trustcacerts -keystore jks/truststore.jks -storetype JKS -storepass changeme -alias CA -file server.pem # TODO This is not CA. 

```

### Install operators

#### Pre-reqs

* Add confluentinc repo

```shell
helm repo add confluentinc https://packages.confluent.io/helm
helm repo update
```

* Create namespace

```shell
kubectl create ns confluent
```

* Add License

```shell
kubectl create secret generic cp-license --from-file=license.txt -n confluent
```

* Add certs

```shell
kubectl create secret generic kafka-tls -n confluent --from-file=fullchain.pem=generated/server.pem --from-file=cacerts.pem=generated/ca.pem --from-file=privkey.pem=generated/server-key.pem

kubectl create configmap cmf-keystore -n confluent --from-file generated/jks/keystore.jks
kubectl create configmap cmf-truststore -n confluent --from-file generated/jks/truststore.jks
```

* Install cert manager and confluent kafka & flink operator

```shell
kubectl create -f https://github.com/jetstack/cert-manager/releases/download/v1.8.2/cert-manager.yaml

helm upgrade --install cp-flink-kubernetes-operator confluentinc/flink-kubernetes-operator -n confluent

helm pull confluentinc/flink-kubernetes-operator 

helm upgrade --install confluent-operator confluentinc/confluent-for-kubernetes --set enableCMFDay2Ops=true -n confluent

```

### Install CP Kafka

TODO

### Install CP Flink

```shell
helm upgrade --install -f local.yaml cmf confluentinc/confluent-manager-for-apache-flink --namespace confluent --set license.secretRef=cp-license

helm inspect values --version 1.0.1 confluentinc/confluent-manager-for-apache-flink

# Add uncommented entry to /etc/hosts
# 127.0.0.1       confluent-manager-for-apache-flink.confluent.svc.cluster.local

curl --cert ./generated/server.pem --key ./generated/server-key.pem --cacert generated/ca.pem  https://confluent-manager-for-apache-flink.confluent.svc.cluster.local:8080/cmf/api/v1/environments

# Create CMFRestClass.yaml
kubectl apply -f CMFRestClass.yaml -n confluent

kubectl describe CMFRestClass/default -n confluent
```

### Run Jobs

* Deploy Flink Jobs using `kubectl` commands

```shell
# TODO k8 namespace preventing deploy env and apps across namespaces

# CPU cannot be less than 1
#   Warning  Warning  2s (x6 over 42s)  flinkapplication  failed to create/update flink application name: flink-app1 namespace: confluent: CMF rest API request failed: https://cmf-service:80/cmf/api/v1/environments/flink-env1/applications: Invalid FlinkApplicationSpec payload: Cannot deserialize value of type `java.lang.Double` from String "500m": not a valid `Double` value
# at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: io.confluent.cmf.kubernetes.cr.FlinkApplicationSpec["jobManager"]->org.apache.flink.kubernetes.operator.api.spec.JobManagerSpec["resource"]->org.apache.flink.kubernetes.operator.api.spec.Resource["cpu"])
#  Warning  Warning  2s  flinkapplication  failed to create/update flink application name: flink-app1 namespace: confluent: CMF rest API request failed: https://cmf-service:80/cmf/api/v1/environments/flink-env1/applications: Invalid FlinkApplicationSpec payload: Cannot deserialize value of type `java.lang.Double` from String "500M": not a valid `Double` value
# at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: io.confluent.cmf.kubernetes.cr.FlinkApplicationSpec["jobManager"]->org.apache.flink.kubernetes.operator.api.spec.JobManagerSpec["resource"]->org.apache.flink.kubernetes.operator.api.spec.Resource["cpu"])

# Mem cannot be less than 1G
# Error {"type":"org.apache.flink.kubernetes.operator.exception.ReconciliationException","message":"org.apache.flink.configuration.IllegalConfigurationException: TaskManager memory configuration failed: Sum of configured Framework Heap Memory (128.000mb (134217728 bytes)), Framework Off-Heap Memory (128.000mb (134217728 bytes)), Task Off-Heap Memory (0 bytes), Managed Memory (100.800mb (105696462 bytes)) and Network Memory (64.000mb (67108864 bytes)) exceed configured Total Flink Memory (252.000mb (264241152 bytes)).","additionalMetadata":{},"throwableList":[{"type":"org.apache.flink.configuration.IllegalConfigurationException","message":"TaskManager memory configuration failed: Sum of configured Framework Heap Memory (128.000mb (134217728 bytes)), Framework Off-Heap Memory (128.000mb (134217728 bytes)), Task Off-Heap Memory (0 bytes), Managed Memory (100.800mb (105696462 bytes)) and Network Memory (64.000mb (67108864 bytes)) exceed configured Total Flink Memory (252.000mb (264241152 bytes)).","additionalMetadata":{}},{"type":"org.apache.flink.configuration.IllegalConfigurationException","message":"Sum of configured Framework Heap Memory (128.000mb (134217728 bytes)), Framework Off-Heap Memory (128.000mb (134217728 bytes)), Task Off-Heap Memory (0 bytes), Managed Memory (100.800mb (105696462 bytes)) and Network Memory (64.000mb (67108864 bytes)) exceed configured Total Flink Memory (252.000mb (264241152 bytes)).","additionalMetadata":{}}]}

# create flinkenvironment.yaml
kubectl apply -f flinkenvironment.yaml

# Env should be in `CREATED` status with no ERROR messages
kubectl get flinkenvironments

# create flinkapplication.yaml
kubectl apply -f flinkapplication.yaml

# App should be in `CREATED` status with no ERROR messages
# It may take a whole to get to this state
# Monitor events and pod logs
kubectl get flinkapplication

kubectl port-forward svc/flink-app1-rest 8081:8081 -n confluent

```

* Deploy Flink Jobs using `confluent` commands

```shell
kubectl port-forward svc/cmf-service 8080:80 -n confluent

confluent flink environment create env1 --url http://localhost:8080 --kubernetes-namespace confluent

confluent flink application create flink_job.json --environment env1 --url http://localhost:8080

confluent flink application web-ui-forward basic-example --environment env1 --port 8090 --url http://localhost:8080


```

### Setup monitoring

TODO

### With AuthZ

