# README

## Pre-requisites

* `minikube` or `k8` cluster
* `mc` with a pvc mount
  * `nohup kubectl port-forward -n minio-dev pod/minio 9000 9090 > minio.nohup.log 2>&1 &` 
* CP Flink Cluster. TODO steps for creating this
  * `nohup kubectl port-forward -n cpf svc/cmf-service 8080:80 > cmf-service.nohup.log 2>&1 &`
  * `export CONFLUENT_CMF_URL=http://localhost:8080`
  * With the above export, you may remove the `-url` option from the `confluent flink` commands
  * Otherwise provide `-url http://localhost:8080`

## Compile and Run

* `mvn clean package`
* `mc cp target/flink-datastream-learn-0.0.1-SNAPSHOT.jar dev-minio/flink/`
* Delete App if exists
  * `curl -X DELETE http://localhost:8080/cmf/api/v1/environments/development/applications/flink-ds-learn`
  * or `confluent flink application delete flink-ds-learn  --environment development`
* change the `<appname>` that you want to run in `kafka-deployment.json`
```
    "job": {
      "jarURI": "local:///opt/flink/downloads/flink-datastream-learn-0.0.1-SNAPSHOT.jar",
      "args": ["<appname>", "--command.config=/opt/flink/downloads/command_config.properties"],
      "state": "running",
      "parallelism": 1,
      "upgradeMode": "stateless"
    }
```
* Create or use appropriate command_config.properties and `mc cp command_config.properties dev-minio/flink/command_config.properties`
* `confluent flink application create --environment development kafka-deployment.json`
* `confluent flink application web-ui-forward --environment development flink-ds-learn`
* `kubectl logs -n flink pod/flink-ds-learn-<pod-id>`