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

# openssl pkcs12 -export -in server.pem -inkey server-key.pem -out jks/server.p12 -name "Server"
openssl pkcs12 -export -passout pass:changeme -in server.pem -inkey server-key.pem -out jks/server.p12 -name "Server"

# keytool -importkeystore -srckeystore jks/server.p12 -srcstoretype pkcs12 -destkeystore jks/keystore.jks
keytool -importkeystore -srcstorepass changeme -deststorepass changeme -destkeypass changeme -srckeystore jks/server.p12 -srcstoretype pkcs12 -destkeystore jks/keystore.jks

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
kubectl create secret generic cp-license --from-file=license.txt=license.txt= -n confluent
kubectl create secret generic cp-license-2 --from-file=license.txt=license.txt -n confluent
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

* Add to `/etc/hosts`

```properties
127.0.0.1 confluent-manager-for-apache-flink confluent-manager-for-apache-flink.confluent.svc.cluster.local
127.0.0.1 flink-app1-rest flink-app1-rest.confluent.svc.cluster.local
127.0.0.1 flink-operator-webhook-service flink-operator-webhook-service.confluent.svc.cluster.local
127.0.0.1 keycloak keycloak.confluent.svc.cluster.local
```

* Install keycloak

```shell
cd ~/github.com/confluentinc/confluent-kubernetes-examples/security/oauth/keycloak

# replace `operator` namespace with `confluent`
kubectl apply -f keycloak_deploy.yaml -n confluent

kubectl port-forward service/keycloak 8080:8080 -n confluent

# base64-endcoded create
echo -n "<client_id>:<secret>" | base64

# Check token with `jwt.io`
curl --location 'http://keycloak:8080/realms/sso_test/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --header 'Authorization: Basic <base64-endcoded>' --data-urlencode 'grant_type=client_credentials'
```

* Create new SSO users using keycload admin UI `http://keycloak:8080/`
  * UserID: admin
  * Password: admin
  * realm: sso_test
  * Users: `flink_client_1`, `flink_client_2`, `flink_client_3`

### Install CP Kafka

```shell
kubectl create -n confluent secret generic oauth-jass --from-file=oauth.txt=oauth_jass.txt

kubectl create secret generic mds-token --from-file=mdsPublicKey.pem=mds-publickey.txt --from-file=mdsTokenKeyPair.pem=mds-tokenkeypair.txt -n confluent

kubectl create secret generic credential --from-file=plain-users.json=creds-kafka-sasl-users.json --from-file=plain.txt=creds-client-kafka-sasl-user.txt --from-file=ldap.txt=ldap.txt -n confluent

kubectl apply -f cp_components.yaml -n confluent
```

#### Validation

```shell

kubectl get pod -n confluent
kubectl exec -it pod/kafka-0 -n confluent -- /bin/bash
```

```properties
cat <<EOF>/tmp/kafka1.properties
# kafka1.properties
sasl.mechanism=OAUTHBEARER
security.protocol=SASL_SSL
sasl.login.callback.handler.class=org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler
sasl.login.connect.timeout.ms=15000
sasl.oauthbearer.token.endpoint.url=http://keycloak:8080/realms/sso_test/protocol/openid-connect/token
sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId="ssologin" clientSecret="KbLRih1HzjDC267PefuKU7QIoZ8hgHDK";
ssl.truststore.location=/mnt/sslcerts/kafka-tls/truststore.p12
ssl.truststore.password=mystorepassword
EOF

cat <<EOF>/tmp/kafka2.properties
# kafka2.properties
sasl.mechanism=OAUTHBEARER
security.protocol=SASL_SSL
sasl.login.callback.handler.class=org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler
sasl.login.connect.timeout.ms=15000
sasl.oauthbearer.token.endpoint.url=http://keycloak:8080/realms/sso_test/protocol/openid-connect/token
sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId="flink_client_1" clientSecret="o2lE80cOb6XHETa5ozyoWHt4MG7fiLaR";
ssl.truststore.location=/mnt/sslcerts/kafka-tls/truststore.p12
ssl.truststore.password=mystorepassword
EOF
```

```shell
kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9073 --list --command-config /tmp/kafka2.properties

kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9073 --topic test-topic-internal-new --create --replication-factor 3 --command-config /tmp/kafka2.properties

kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9073 --topic test-topic-internal --create --replication-factor 3 --command-config /tmp/kafka1.properties
kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9094 --list --command-config /tmp/kafka1.properties

kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9092 --topic test-topic-external --create --replication-factor 3 --command-config /tmp/kafka1.properties
kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9072 --topic test-topic-replication --create --replication-factor 3 --command-config /tmp/kafka1.properties
kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9094 --topic test-topic-custom --create --replication-factor 3 --command-config /tmp/kafka1.properties

kafka-topics --bootstrap-server kafka.confluent.svc.cluster.local:9094 --list --command-config /tmp/kafka1.properties
```

### Install CP Flink with security

```shell
helm upgrade --install -f flink_security.yaml cmf confluentinc/confluent-manager-for-apache-flink --namespace confluent --set license.secretRef=cp-license

helm inspect values --version 1.0.1 confluentinc/confluent-manager-for-apache-flink

curl --cert ./generated/server.pem --key ./generated/server-key.pem --cacert generated/ca.pem  https://confluent-manager-for-apache-flink.confluent.svc.cluster.local:8080/cmf/api/v1/environments

# Create CMFRestClass.yaml
kubectl apply -f CMFRestClass.yaml -n confluent

kubectl describe CMFRestClass/default -n confluent
```

### Run Jobs

* Make sure you have all the latest confluentrolebindings from `cp_components.yaml`

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

* Deploy Flink Jobs using `confluent` commands (not recommended for cp-flink production)

```shell
kubectl port-forward svc/cmf-service 8080:80 -n confluent

confluent flink environment create env1 --url http://localhost:8080 --kubernetes-namespace confluent

confluent flink application create flink_job.json --environment env1 --url http://localhost:8080

confluent flink application web-ui-forward basic-example --environment env1 --port 8090 --url http://localhost:8080
```

### Setup monitoring

TODO