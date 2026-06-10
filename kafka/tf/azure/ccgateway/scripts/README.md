# CCGateway Scripts

## Notes

* Generates self signed certs
* Sets up CCGateway in AKS with DR Failover

```
| -------------------------------------- Customer Azure EastUS ------------------------------------------------------- | ------------Confluent Azure----------- |
                                     | ---------------------- AKS ---------------------------------------------------- |
Client --> GatewayDNS (PrivateZone) --> GatewayLB (k8) --> CCGateway (AKS) --> EastDNS (PrivateZone) --> EastEndpoint --> EastPLService --> EastCluster (CC)
                                                               |
                                                               |-------------> WestDNS (PrivateZone) --> WestEndpoint --> WestPLService --> WestCluster (CC)
```

```
| -------------------------------------- Customer Azure WestUS ------------------------------------------------------- | ------------Confluent Azure----------- |
                                     | ---------------------- AKS ---------------------------------------------------- |
Client --> GatewayDNS (PrivateZone) --> GatewayLB (k8) --> CCGateway (AKS) --> WestDNS (PrivateZone) --> WestEndpoint --> WestPLService --> WestCluster (CC)
                                                               |
                                                               |-------------> EastDNS (PrivateZone) --> EastEndpoint --> EastPLService --> EastCluster (CC)
```

* Run scripts for one region and Validate Connectivity
* Run scripts for other region and Validate Connectivity

## Steps

### Login using az client and get aks context

* az login
* az account set --subscription <subscription>
* az aks get-credentials --resource-group <rg> --name <cluster> --overwrite-existing

### Generate self signed certificates

* Do this step only once. If run again, you will be required to redo the whole setup as certs will change on subsequent runs and secrets may get corrupted
* server certs are good for 1 year
* use `destroy.sh` to destroy the whole setup except for private DNS that you create in later steps
* run `./generate_certs.sh`

### Download Confluent certs

* Do this step only once. There is no consequence to running this multiple times (waste of time)
* clone `gateway-images` repo -- `git clone git@github.com:confluentinc/gateway-images.git`. `download-certificates.sh` is under `examples/simple-passthrough-to-confluent-cloud`
* Acquire Primary and DR CC Cluster certifcates using `download-certificates.sh` and store them under `../generated/ssl/<bootstrap>`
* Modify truststore.password file to include `jksPassword=confluent`

### Deploy CCGateway for Active/DR capability

* Create `../generated/client-primary.properties` and `../generated/client-dr.properties`
* Run `./deploy.sh`
* Gather LB_IP `kubectl get svc/confluent-gateway-bootstrap-lb -n confluent`
* Use `CLUSTER_IP` to update any PrivateDNS recordset within AKS cluster
* Use `EXTERNAL_IP` to update any PrivateDNS recordset extrenal to AKS cluster

### Create Private DNS Zones

* Create 2 private DNS zones
  * One private DNS (if needed) for resolving gateway DNS within AKS Cluster
  * One (or many -- 1 per VNET) private DNS for resolving gateway DNS external to AKS Cluster
* Create 2 records
  * `ccgateway.kafka.venky.com` and use the LB IP from above. Make sure to change TTL to 1 minute (else wait time for changes is more)
  * `*.ccgateway.kafka.venky.com` and use the LB IP from above. Make sure to change TTL to 1 minute (else wait time for changes is more)
* Make sure to link the VNET with private DNS zone
* TODO: script this in future

### Validation

#### To validate within AKS cluster

* Exec into `kafka-tools` pod -- `kubectl exec -it -n confluent pod/kafka-tools -- /bin/bash`
* Create `/tmp/client-useast2.properties` with kafka client config for USEAST2 cluster
* Create `/tmp/client-uswest2.properties` with kafka client config for USWEST2 cluster
* Execute below commands in `kafka-tools` pod
```
kafka-topics --bootstrap-server ccgateway.kafka.venky.com:9092 --command-config /etc/kafka/client-primary/client-primary.properties --list

kafka-producer-perf-test --topic cpctest --num-records 100 --record-size 10 --throughput 10 --producer.config /etc/kafka/client-primary/client-primary.properties --producer-props bootstrap.servers=ccgateway.kafka.venky.com:9092

kafka-topics --bootstrap-server lkc-q0ygk2.dom4glleo0g.eastus2.azure.confluent.cloud:9092 --command-config /tmp/client-useast2.properties --list

kafka-topics --bootstrap-server lkc-q0ygk2.dom4glleo0g.eastus2.azure.confluent.cloud:9092 --command-config /etc/kafka/client-primary/client-primary.properties --list

kafka-topics --bootstrap-server lkc-orwo89.dom4glle57g.westus2.azure.confluent.cloud:9092 --command-config /tmp/client-uswest2.properties --list

kafka-topics --bootstrap-server lkc-orwo89.dom4glle57g.westus2.azure.confluent.cloud:9092 --command-config /etc/kafka/client-dr/client-dr.properties --list
```

#### To validate within AKS cluster

* Exec into `kafka-tools` pod -- `kubectl exec -it -n confluent pod/kafka-tools -- /bin/bash`
* Create `/tmp/client-useast2.properties` with kafka client config for USEAST2 cluster
* Create `/tmp/client-uswest2.properties` with kafka client config for USWEST2 cluster
* Execute below commands in `kafka-tools` pod
```
kafka-topics --bootstrap-server ccgateway.kafka.venky.com:9092 --command-config /etc/kafka/client-primary/client-primary.properties --list
```
