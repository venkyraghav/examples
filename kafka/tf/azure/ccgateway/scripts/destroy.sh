#!/bin/bash
kubectl -n confluent delete -f kafka-tools.yaml
kubectl -n confluent delete -f gateway.yaml
kubectl -n confluent delete secret gateway-tls
kubectl -n confluent delete secret cc-primary-tls
kubectl -n confluent delete secret cc-dr-tls
kubectl -n confluent delete secret client-tls
kubectl -n confluent delete secret client-primary
kubectl -n confluent delete secret client-dr
kubectl delete namespace confluent
