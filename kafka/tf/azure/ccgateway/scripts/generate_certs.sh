#!/bin/bash

ssl_dir=../generated/ssl
ssl_gateway_dir=${ssl_dir}/gateway
mkdir -p ${ssl_gateway_dir}

ca_key=${ssl_gateway_dir}/ca-key.pem
openssl genrsa -out ${ca_key} 2048

ca_cert=${ssl_gateway_dir}/ca-cert.pem
openssl req -x509 -new -nodes -key ${ca_key} -sha256 -days 3650 -out ${ca_cert} \
    -subj "/C=US/ST=CA/L=Mountain View/O=Confluent/OU=Engineering/CN=Gateway Test CA"

privkey=${ssl_gateway_dir}/privkey.pem
openssl genrsa -out ${privkey} 2048

gateway_csr=${ssl_gateway_dir}/csr.pem
openssl req -new -key ${privkey} -out ${gateway_csr} \
    -subj "/C=US/ST=CA/L=Mountain View/O=Confluent/OU=Engineering/CN=ccgateway.kafka.venky.com"

gateway_fqdn="ccgateway.kafka.venky.com"
gateway_cert=${ssl_gateway_dir}/gateway.pem
openssl x509 -req -in ${gateway_csr} -CA ${ca_cert} -CAkey ${ca_key} -CAcreateserial \
    -out ${gateway_cert} -days 365 -sha256 -extensions v3_req \
    -extfile <(
        printf "[v3_req]\n"
        printf "authorityKeyIdentifier=keyid,issuer\n"
        printf "basicConstraints=CA:FALSE\n"
        printf "keyUsage=digitalSignature,nonRepudiation,keyEncipherment,dataEncipherment\n"
        printf "subjectAltName=DNS:${gateway_fqdn}\n"
    )

rm -f ${gateway_csr}

fullchain_cert=${ssl_gateway_dir}/fullchain.pem
cat ${gateway_cert} ${ca_cert} > ${fullchain_cert}

storepass=confluent
gateway_truststore=${ssl_gateway_dir}/gateway-truststore.jks
keytool -keystore ${gateway_truststore} -alias CARoot -import -file ${ca_cert} -storepass ${storepass} -noprompt

# echo "NOTE: Acquire Primary and DR CC Cluster certifcates using download-certificates.sh"
