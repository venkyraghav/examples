# Trogdor Setup

## Local only setup

### Config file: trogdor-local.conf

```json
{
    "_comment": [
        "Setup trogdor in my local cluster"
    ],
    "platform": "org.apache.kafka.trogdor.basic.BasicPlatform", "nodes": {
        "coord": {
            "hostname": "192.168.50.119",
            "trogdor.coordinator.port": 8889
        },
        "agent1": {
            "hostname": "192.168.50.119",
            "trogdor.coordinator.port": 8889,
            "trogdor.agent.port": 8188
        },
        "agent2": {
            "hostname": "192.168.50.119",
            "trogdor.coordinator.port": 8889,
            "trogdor.agent.port": 8288
        }
    }
}
```

### Start processes


```shell
trogdor agent -c trogdor-local.conf -n agent1 &> /tmp/trogdor-agent-agent1.log &
```

```shell
trogdor agent-client status --target 192.168.50.119:8188

Agent is running at 192.168.50.119:8188.
    Start time: 2024-05-14T10:00:54.089-04:00
WORKER_ID TASK_ID STATE TASK_TYPE
```

```shell
trogdor agent -c trogdor-local.conf -n agent2 &> /tmp/trogdor-agent-agent2.log &
```

```shell
trogdor agent-client status --target 192.168.50.119:8288

Agent is running at 192.168.50.119:8288.
    Start time: 2024-05-14T10:01:16.165-04:00
WORKER_ID TASK_ID STATE TASK_TYPE
```

```shell
trogdor coordinator -c trogdor-local.conf -n coord &> /tmp/trogdor-coordinator.log &
```

```shell
trogdor client status --target 192.168.50.119:8889

Coordinator is running at 192.168.50.119:8889.
    Start time: 2024-05-14T10:01:25.819-04:00
```

### Send Load

```shell
trogdor client createTask --target 192.168.50.119:8889 -i txnproduce0 --spec spec/txn_produce_bench.json;trogdor client showTask --target 192.168.50.119:8889 -i txnproduce0 --show-status
Sent CreateTaskRequest for task txnproduce0.
Task txnproduce0 of type org.apache.kafka.trogdor.workload.ProduceBenchSpec is RUNNING. Started 2024-05-14T11:52:01.157-04:00; will stop after 10s
Status: "receiving"
```

```shell
trogdor agent-client status --target 192.168.50.119:8288
Agent is running at 192.168.50.119:8288.
	Start time: 2024-05-14T10:01:16.165-04:00
WORKER_ID          TASK_ID     STATE      TASK_TYPE
544036199546764975 txnproduce0 WorkerDone org.apache.kafka.trogdor.workload.ProduceBenchSpec
```

```shell
trogdor agent-client status --target 192.168.50.119:8188
Agent is running at 192.168.50.119:8188.
	Start time: 2024-05-14T10:00:54.089-04:00
WORKER_ID          TASK_ID  STATE      TASK_TYPE
544036199546764973 produce0 WorkerDone org.apache.kafka.trogdor.workload.ProduceBenchSpec
544036199546764974 produce1 WorkerDone org.apache.kafka.trogdor.workload.ProduceBenchSpec
```

```shell
trogdor client showTask --target 192.168.50.119:8889 -i produce0 --show-status -v
Task produce0 of type org.apache.kafka.trogdor.workload.ProduceBenchSpec is DONE. FINISHED at 2024-05-14T10:23:53.903-04:00 after 6s
Spec: {
  "class" : "org.apache.kafka.trogdor.workload.ProduceBenchSpec",
  "startMs" : 1715696627853,
  "durationMs" : 10000,
  "producerNode" : "agent1",
  "bootstrapServers" : "192.168.50.119:9091",
  "targetMessagesPerSec" : 100,
  "maxMessages" : 500,
  "keyGenerator" : {
    "type" : "sequential",
    "size" : 4,
    "startOffset" : 0
  },
  "valueGenerator" : {
    "type" : "constant",
    "size" : 512,
    "value" : "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
  },
  "activeTopics" : {
    "foo[1-3]" : {
      "numPartitions" : 10,
      "replicationFactor" : 1
    }
  },
  "inactiveTopics" : {
    "foo[4-5]" : {
      "numPartitions" : 10,
      "replicationFactor" : 1
    }
  },
  "useConfiguredPartitioner" : false,
  "skipFlush" : false
}

Status: {
  "totalSent" : 500,
  "averageLatencyMs" : 4.208,
  "p50LatencyMs" : 3,
  "p95LatencyMs" : 11,
  "p99LatencyMs" : 15,
  "transactionsCommitted" : 0
}
```

## Trogdor Cluster
