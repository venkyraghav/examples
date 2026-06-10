#!/bin/bash
ssl_dir=../generated/ssl
ssl_gateway_dir=${ssl_dir}/gateway

ca_cert=${ssl_gateway_dir}/ca-cert.pem
privkey=${ssl_gateway_dir}/privkey.pem
fullchain_cert=${ssl_gateway_dir}/fullchain.pem
gateway_fqdn="ccgateway.kafka.venky.com"
#primary_cluster="pkc-56d1g.eastus.azure.confluent.cloud"
#dr_cluster="pkc-921jm.us-east-2.aws.confluent.cloud"

helm repo add confluentinc https://packages.confluent.io/helm
helm repo update

kubectl create namespace confluent

helm upgrade --install confluent-operator confluentinc/confluent-for-kubernetes -n confluent

kubectl -n confluent create secret generic gateway-tls --from-file=fullchain.pem=${fullchain_cert} \
    --from-file=cacerts.pem=${ca_cert} --from-file=privkey.pem=${privkey}

# kubectl -n confluent create secret generic cc-primary-tls \
#      --from-file=truststore.jks=${ssl_dir}/${primary_cluster}/truststore.p12 \
#      --from-file=jksPassword.txt=${ssl_dir}/${primary_cluster}/truststore.password

# kubectl -n confluent create secret generic cc-dr-tls \
#      --from-file=truststore.jks=${ssl_dir}/${dr_cluster}/truststore.p12 \
#      --from-file=jksPassword.txt=${ssl_dir}/${dr_cluster}/truststore.password

kubectl -n confluent create secret generic client-tls \
     --from-file=truststore.jks=${ssl_gateway_dir}/gateway-truststore.jks \
     --from-literal=truststore.password='confluent'

kubectl -n confluent create secret generic client-primary \
     --from-file=client-primary.properties=../generated/client-primary.properties

kubectl -n confluent create secret generic client-dr \
     --from-file=client-dr.properties=../generated/client-dr.properties

kubectl -n confluent apply -f gateway.yaml

kubectl -n confluent apply -f kafka-tools.yaml
