export bootstrap=pkc-921jm.us-east-2.aws.confluent.cloud
rm -Rf  ../generated/ssl/${bootstrap}
~/github.com/confluentinc/code/gateway-images/examples/simple-passthrough-to-confluent-cloud/download-cc-certs.sh -d ../generated/ssl/${bootstrap} ${bootstrap}:9092

export bootstrap=pkc-56d1g.eastus.azure.confluent.cloud
rm -Rf  ../generated/ssl/${bootstrap}
~/github.com/confluentinc/code/gateway-images/examples/simple-passthrough-to-confluent-cloud/download-cc-certs.sh -d ../generated/ssl/${bootstrap} ${bootstrap}:9092
