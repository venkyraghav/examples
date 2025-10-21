package main

import (
	"encoding/json"
	"net/http"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

type WindowStatistics struct {
	Min        int   `json:"min"`
	Max        int   `json:"max"`
	Avg        int   `json:"avg"`
	Sum        int64 `json:"sum"`
	Stddev     int   `json:"stddev"`
	P50        int   `json:"p50"`
	P75        int   `json:"p75"`
	P90        int   `json:"p90"`
	P95        int   `json:"p95"`
	P99        int   `json:"p99"`
	P9999      int   `json:"p99_99"`
	Outofrange int   `json:"outofrange"`
	Hdrsize    int   `json:"hdrsize"`
	Cnt        int   `json:"cnt"`
}

type CgrpStats struct {
	State           string `json:"state"`
	JoinState       string `json:"join_state"`
	RebalanceAge    int    `json:"rebalance_age"`
	RebalanceCnt    int    `json:"rebalance_cnt"`
	RebalanceReason string `json:"rebalance_reason"`
	AssignmentSize  int    `json:"assignment_size"`
}

type EosStats struct {
	IdempState    string `json:"idemp_state"`
	TxnState      string `json:"txn_state"`
	TxnMayEnq     bool   `json:"txn_may_enq"`
	ProducerEpoch int    `json:"producer_epoch"`
	ProducerID    int64  `json:"producer_id"`
}

type Req struct {
	Produce                   int `json:"Produce"`
	ListOffsets               int `json:"ListOffsets"`
	Metadata                  int `json:"Metadata"`
	FindCoordinator           int `json:"FindCoordinator"`
	SaslHandshake             int `json:"SaslHandshake"`
	ApiVersion                int `json:"ApiVersion"`
	InitProducerId            int `json:"InitProducerId"`
	AddPartitionsToTxn        int `json:"AddPartitionsToTxn"`
	AddOffsetsToTxn           int `json:"AddOffsetsToTxn"`
	EndTxn                    int `json:"EndTxn"`
	TxnOffsetCommit           int `json:"TxnOffsetCommit"`
	SaslAuthenticate          int `json:"SaslAuthenticate"`
	DescribeCluster           int `json:"DescribeCluster"`
	DescribeProducers         int `json:"DescribeProducers"`
	DescribeTransactions      int `json:"DescribeTransactions"`
	ListTransactions          int `json:"ListTransactions"`
	GetTelemetrySubscriptions int `json:"GetTelemetrySubscriptions"`
	PushTelemetry             int `json:"PushTelemetry"`
}

type Partition struct {
	Partition       int    `json:"partition"`
	Broker          int    `json:"broker"`
	Leader          int    `json:"leader"`
	Desired         bool   `json:"desired"`
	Unknown         bool   `json:"unknown"`
	MsgqCnt         int    `json:"msgq_cnt"`
	MsgqBytes       int    `json:"msgq_bytes"`
	XmitMsgqCnt     int    `json:"xmit_msgq_cnt"`
	XmitMsgqBytes   int    `json:"xmit_msgq_bytes"`
	FetchqCnt       int    `json:"fetchq_cnt"`
	FetchqSize      int    `json:"fetchq_size"`
	FetchState      string `json:"fetch_state"`
	QueryOffset     int    `json:"query_offset"`
	NextOffset      int    `json:"next_offset"`
	AppOffset       int    `json:"app_offset"`
	StoredOffset    int    `json:"stored_offset"`
	CommitedOffset  int    `json:"commited_offset"`
	CommittedOffset int    `json:"committed_offset"`
	EOFOffset       int    `json:"eof_offset"`
	LoOffset        int    `json:"lo_offset"`
	HiOffset        int    `json:"hi_offset"`
	ConsumerLag     int    `json:"consumer_lag"`
	Txmsgs          int    `json:"txmsgs"`
	Txbytes         int    `json:"txbytes"`
	Rxmsgs          int    `json:"rxmsgs"`
	Rxbytes         int    `json:"rxbytes"`
	Msgs            int    `json:"msgs"`
	RxVerDrops      int    `json:"rx_ver_drops"`
	MsgsInflight    int    `json:"msgs_inflight"`
	NextAckSeq      int    `json:"next_ack_seq"`
	NextErrSeq      int    `json:"next_err_seq"`
	AckedMsgId      int    `json:"acked_msgid"`
}

type Topic struct {
	TopicName   string               `json:"topic"`
	MsgCnt      int                  `json:"msg_cnt"`
	MetadataAge int                  `json:"metadata_age"`
	Batchsize   WindowStatistics     `json:"batchsize"`
	Batchcnt    WindowStatistics     `json:"batchcnt"`
	Partitions  map[string]Partition `json:"partitions"`
}

type Broker struct {
	Name           string           `json:"name"`
	Nodeid         int              `json:"nodeid"`
	Nodename       string           `json:"nodename"`
	Source         string           `json:"source"`
	State          string           `json:"state"`
	Stateage       int              `json:"stateage"`
	OutbufCnt      int              `json:"outbuf_cnt"`
	OutbufMsgCnt   int              `json:"outbuf_msg_cnt"`
	WaitrespCnt    int              `json:"waitresp_cnt"`
	WaitrespMsgCnt int              `json:"waitresp_msg_cnt"`
	Tx             int              `json:"tx"`
	Txbytes        int              `json:"txbytes"`
	Txerrs         int              `json:"txerrs"`
	Txretries      int              `json:"txretries"`
	Txidle         int              `json:"txidle"`
	ReqTimeouts    int              `json:"req_timeouts"`
	Rx             int              `json:"rx"`
	Rxbytes        int              `json:"rxbytes"`
	Rxerrs         int              `json:"rxerrs"`
	Rxcorriderrs   int              `json:"rxcorriderrs"`
	Rxpartial      int              `json:"rxpartial"`
	Rxidle         int              `json:"rxidle"`
	ZbufGrow       int              `json:"zbuf_grow"`
	BufGrow        int              `json:"buf_grow"`
	Wakeups        int              `json:"wakeups"`
	Connects       int              `json:"connects"`
	Disconnects    int              `json:"disconnects"`
	IntLatency     WindowStatistics `json:"int_latency"`
	OutbufLatency  WindowStatistics `json:"outbuf_latency"`
	Rtt            WindowStatistics `json:"rtt"`
	Throttle       WindowStatistics `json:"throttle"`
	Req
}

type KafkaStatistics struct {
	Name             string            `json:"name"`
	ClientID         string            `json:"client_id"`
	Type             string            `json:"type"`
	Ts               int64             `json:"ts"`
	Time             int               `json:"time"`
	Age              int               `json:"age"`
	Replyq           int               `json:"replyq"`
	MsgCnt           int               `json:"msg_cnt"`
	MsgSize          int               `json:"msg_size"`
	MsgMax           int               `json:"msg_max"`
	MsgSizeMax       int               `json:"msg_size_max"`
	SimpleCnt        int               `json:"simple_cnt"`
	MetadataCacheCnt int               `json:"metadata_cache_cnt"`
	Brokers          map[string]Broker `json:"brokers"`
	Topics           map[string]Topic  `json:"topics"`
	Tx               int               `json:"tx"`
	TxMsgs           int               `json:"txmsgs"`
	TxBytes          int               `json:"tx_bytes"`
	TxMsgsBytes      int               `json:"txmsg_bytes"`
	Rx               int               `json:"rx"`
	RxBytes          int               `json:"rx_bytes"`
	RxMsgs           int               `json:"rxmsgs"`
	RxMsgsBytes      int               `json:"rxmsg_bytes"`
	Cgrp             CgrpStats         `json:"cgrp"`
	Eos              EosStats          `json:"eos"`
}

type PrometheusStatistics struct {
	Time             *prometheus.GaugeVec
	Age              *prometheus.GaugeVec
	Replyq           *prometheus.GaugeVec
	MsgCnt           *prometheus.GaugeVec
	MsgMax           *prometheus.GaugeVec
	Tx               *prometheus.GaugeVec
	TxMsgs           *prometheus.GaugeVec
	TxMsgsBytes      *prometheus.GaugeVec
	Rx               *prometheus.GaugeVec
	RxMsgs           *prometheus.GaugeVec
	RxMsgsBytes      *prometheus.GaugeVec
	MetadataCacheCnt *prometheus.GaugeVec
	Ts               *prometheus.GaugeVec
	MsgSize          *prometheus.GaugeVec
	TxBytes          *prometheus.GaugeVec
	RxBytes          *prometheus.GaugeVec

	// Cgrp
	CgrpState           *prometheus.GaugeVec
	CgrpJoinState       *prometheus.GaugeVec
	CgrpRebalanceAge    *prometheus.GaugeVec
	CgrpRebalanceCnt    *prometheus.GaugeVec
	CgrpRebalanceReason *prometheus.GaugeVec
	CgrpAssignmentSize  *prometheus.GaugeVec
	// EOS
	EosIdempState    *prometheus.GaugeVec
	EosTxnState      *prometheus.GaugeVec
	EosTxnMayEnq     *prometheus.GaugeVec
	EosProducerEpoch *prometheus.GaugeVec
	EosProducerID    *prometheus.GaugeVec

	BrokerOutbufCnt                    *prometheus.GaugeVec
	BrokerOutbufMsgCnt                 *prometheus.GaugeVec
	BrokerWaitrespCnt                  *prometheus.GaugeVec
	BrokerWaitrespMsgCnt               *prometheus.GaugeVec
	BrokerTx                           *prometheus.GaugeVec
	BrokerTxbytes                      *prometheus.GaugeVec
	BrokerTxerrs                       *prometheus.GaugeVec
	BrokerTxretries                    *prometheus.GaugeVec
	BrokerTxidle                       *prometheus.GaugeVec
	BrokerReqTimeouts                  *prometheus.GaugeVec
	BrokerRx                           *prometheus.GaugeVec
	BrokerRxbytes                      *prometheus.GaugeVec
	BrokerRxerrs                       *prometheus.GaugeVec
	BrokerRxcorriderrs                 *prometheus.GaugeVec
	BrokerRxpartial                    *prometheus.GaugeVec
	BrokerZbufGrow                     *prometheus.GaugeVec
	BrokerWakeups                      *prometheus.GaugeVec
	BrokerRxIdle                       *prometheus.GaugeVec
	BrokerConnects                     *prometheus.GaugeVec
	BrokerDisconnects                  *prometheus.GaugeVec
	BrokerIntLatencyMin                *prometheus.GaugeVec
	BrokerIntLatencyMax                *prometheus.GaugeVec
	BrokerIntLatencyAvg                *prometheus.GaugeVec
	BrokerIntLatency99                 *prometheus.GaugeVec
	BrokerOutbufLatencyMin             *prometheus.GaugeVec
	BrokerOutbufLatencyMax             *prometheus.GaugeVec
	BrokerOutbufLatencyAvg             *prometheus.GaugeVec
	BrokerOutbufLatency99              *prometheus.GaugeVec
	BrokerRttMin                       *prometheus.GaugeVec
	BrokerRttMax                       *prometheus.GaugeVec
	BrokerRttAvg                       *prometheus.GaugeVec
	BrokerRtt99                        *prometheus.GaugeVec
	BrokerThrottleMin                  *prometheus.GaugeVec
	BrokerThrottleMax                  *prometheus.GaugeVec
	BrokerThrottleAvg                  *prometheus.GaugeVec
	BrokerThrottle99                   *prometheus.GaugeVec
	BrokerReqProduce                   *prometheus.GaugeVec
	BrokerReqListOffsets               *prometheus.GaugeVec
	BrokerReqMetadata                  *prometheus.GaugeVec
	BrokerReqFindCoordinator           *prometheus.GaugeVec
	BrokerReqSaslHandshake             *prometheus.GaugeVec
	BrokerReqApiVersion                *prometheus.GaugeVec
	BrokerReqInitProducerId            *prometheus.GaugeVec
	BrokerReqAddPartitionsToTxn        *prometheus.GaugeVec
	BrokerReqAddOffsetsToTxn           *prometheus.GaugeVec
	BrokerReqEndTxn                    *prometheus.GaugeVec
	BrokerReqTxnOffsetCommit           *prometheus.GaugeVec
	BrokerReqSaslAuthenticate          *prometheus.GaugeVec
	BrokerReqDescribeCluster           *prometheus.GaugeVec
	BrokerReqDescribeProducers         *prometheus.GaugeVec
	BrokerReqDescribeTransactions      *prometheus.GaugeVec
	BrokerReqListTransactions          *prometheus.GaugeVec
	BrokerReqGetTelemetrySubscriptions *prometheus.GaugeVec
	BrokerReqPushTelemetry             *prometheus.GaugeVec

	// Add Topic
	TopicMetadataAge              *prometheus.GaugeVec
	TopicBatchsizeMin             *prometheus.GaugeVec
	TopicBatchsizeMax             *prometheus.GaugeVec
	TopicBatchsizeAvg             *prometheus.GaugeVec
	TopicBatchsize99              *prometheus.GaugeVec
	TopicBatchcntMin              *prometheus.GaugeVec
	TopicBatchcntMax              *prometheus.GaugeVec
	TopicBatchcntAvg              *prometheus.GaugeVec
	TopicBatchcnt99               *prometheus.GaugeVec
	TopicPartitionBroker          *prometheus.GaugeVec
	TopicPartitionLeader          *prometheus.GaugeVec
	TopicPartitionDesired         *prometheus.GaugeVec
	TopicPartitionMsgqCnt         *prometheus.GaugeVec
	TopicPartitionMsgqBytes       *prometheus.GaugeVec
	TopicPartitionXmitMsgqCnt     *prometheus.GaugeVec
	TopicPartitionXmitMsgqBytes   *prometheus.GaugeVec
	TopicPartitionFetchqCnt       *prometheus.GaugeVec
	TopicPartitionFetchqSize      *prometheus.GaugeVec
	TopicPartitionFetchState      *prometheus.GaugeVec
	TopicPartitionQueryOffset     *prometheus.GaugeVec
	TopicPartitionNextOffset      *prometheus.GaugeVec
	TopicPartitionAppOffset       *prometheus.GaugeVec
	TopicPartitionStoredOffset    *prometheus.GaugeVec
	TopicPartitionCommittedOffset *prometheus.GaugeVec
	TopicPartitionEOFOffset       *prometheus.GaugeVec
	TopicPartitionLoOffset        *prometheus.GaugeVec
	TopicPartitionHiOffset        *prometheus.GaugeVec
	TopicPartitionConsumerLag     *prometheus.GaugeVec
	TopicPartitionTxmsgs          *prometheus.GaugeVec
	TopicPartitionTxbytes         *prometheus.GaugeVec
	TopicPartitionRxmsgs          *prometheus.GaugeVec
	TopicPartitionRxbytes         *prometheus.GaugeVec
	TopicPartitionMsgs            *prometheus.GaugeVec
	TopicPartitionRxVerDrops      *prometheus.GaugeVec
	TopicPartitionMsgsInflight    *prometheus.GaugeVec
	TopicPartitionNextAckSeq      *prometheus.GaugeVec
	TopicPartitionNextErrSeq      *prometheus.GaugeVec
	TopicPartitionAckedMsgId      *prometheus.GaugeVec
}

var prometheusStatistics *PrometheusStatistics

func newPromGaugeVec(name string, help string, labels []string) *prometheus.GaugeVec {
	return prometheus.NewGaugeVec(prometheus.GaugeOpts{Name: name, Help: help}, labels)
}

func GetPrometheusStatistics() *PrometheusStatistics {
	if prometheusStatistics == nil {
		prometheusStatistics = &PrometheusStatistics{
			Time:             newPromGaugeVec("kafka_time", "Time in microseconds", []string{"name", "client_id", "type"}),
			Age:              newPromGaugeVec("kafka_age", "Age in microseconds", []string{"name", "client_id", "type"}),
			Replyq:           newPromGaugeVec("kafka_replyq", "Number of ops (callbacks, events, etc) waiting in queue for application to serve with rd_kafka_poll()", []string{"name", "client_id", "type"}),
			MsgCnt:           newPromGaugeVec("kafka_msg_cnt", "Messages currently queued in producer", []string{"name", "client_id", "type"}),
			MsgMax:           newPromGaugeVec("kafka_msg_max", "maximum number of messages allowed allowed on the producer queues", []string{"name", "client_id", "type"}),
			Tx:               newPromGaugeVec("kafka_tx", "Total number of requests sent to Kafka brokers", []string{"name", "client_id", "type"}),
			TxMsgs:           newPromGaugeVec("kafka_tx_msgs_total", "Total messages sent", []string{"name", "client_id", "type"}),
			TxMsgsBytes:      newPromGaugeVec("kafka_tx_msgs_bytes", "Total number of message bytes (including framing, such as per-Message framing and MessageSet/batch framing) transmitted to Kafka brokers", []string{"name", "client_id", "type"}),
			Rx:               newPromGaugeVec("kafka_rx", "Total number of responses received from Kafka brokers", []string{"name", "client_id", "type"}),
			RxMsgs:           newPromGaugeVec("kafka_rx_msgs_total", "Total messages received", []string{"name", "client_id", "type"}),
			RxMsgsBytes:      newPromGaugeVec("kafka_rx_msgs_bytes", "Total number of message bytes (including framing) received from Kafka brokers", []string{"name", "client_id", "type"}),
			MetadataCacheCnt: newPromGaugeVec("kafka_metadata_cache_cnt", "Number of topics in the metadata cache", []string{"name", "client_id", "type"}),
			Ts:               newPromGaugeVec("kafka_ts", "librdkafka's internal monotonic clock (microseconds)", []string{"name", "client_id", "type"}),
			MsgSize:          newPromGaugeVec("kafka_msg_size", "Current total size of messages in producer queues", []string{"name", "client_id", "type"}),
			TxBytes:          newPromGaugeVec("kafka_tx_bytes", "Total number of bytes transmitted to Kafka brokers", []string{"name", "client_id", "type"}),
			RxBytes:          newPromGaugeVec("kafka_rx_bytes", "Total number of bytes received from Kafka brokers", []string{"name", "client_id", "type"}),

			// Cgrp
			CgrpState:           newPromGaugeVec("kafka_cgrp_state", "Local consumer group handler's state.", []string{"name", "client_id", "type", "state"}),
			CgrpJoinState:       newPromGaugeVec("kafka_cgrp_joinstate", "Local consumer group handler's join state.", []string{"name", "client_id", "type", "joinstate"}),
			CgrpRebalanceAge:    newPromGaugeVec("kafka_cgrp_rebalance_age", "Time elapsed since last rebalance (assign or revoke) (milliseconds).", []string{"name", "client_id", "type"}),
			CgrpRebalanceCnt:    newPromGaugeVec("kafka_cgrp_rebalance_cnt", "Total number of rebalances (assign or revoke).", []string{"name", "client_id", "type"}),
			CgrpRebalanceReason: newPromGaugeVec("kafka_cgrp_rebalance_reason", "Last rebalance reason, or empty string.", []string{"name", "client_id", "type", "reason"}),
			CgrpAssignmentSize:  newPromGaugeVec("kafka_cgrp_assignment_size", "Current assignment's partition count.", []string{"name", "client_id", "type"}),
			// EOS
			EosIdempState:    newPromGaugeVec("kafka_eos_idemp_state", "Current idempotent producer id state.", []string{"name", "client_id", "type", "idempstate"}),
			EosTxnState:      newPromGaugeVec("kafka_eos_txn_state", "Current transactional producer state.", []string{"name", "client_id", "type", "state"}),
			EosTxnMayEnq:     newPromGaugeVec("kafka_eos_txn_mayenq", "Transactional state allows enqueuing (producing) new messages.", []string{"name", "client_id", "type"}),
			EosProducerEpoch: newPromGaugeVec("kafka_eos_producer_epoch", "The current epoch (or -1).", []string{"name", "client_id", "type"}),
			EosProducerID:    newPromGaugeVec("kafka_eos_producerid", "	The currently assigned Producer ID (or -1).", []string{"name", "client_id", "type"}),

			// Broker
			BrokerOutbufCnt:                    newPromGaugeVec("kafka_broker_outbuf_cnt", "Number of requests awaiting transmission to broker", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerOutbufMsgCnt:                 newPromGaugeVec("kafka_broker_outbuf_msg_cnt", "Number of messages awaiting transmission to broker", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerWaitrespCnt:                  newPromGaugeVec("kafka_broker_waitresp_cnt", "Number of requests in-flight to broker awaiting response", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerWaitrespMsgCnt:               newPromGaugeVec("kafka_broker_waitresp_msg_cnt", "Number of messages in-flight to broker awaiting response", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerTx:                           newPromGaugeVec("kafka_broker_tx", "Total number of requests sent", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerTxbytes:                      newPromGaugeVec("kafka_broker_tx_bytes", "Total number of bytes sent", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerTxerrs:                       newPromGaugeVec("kafka_broker_tx_errs", "Total number of transmission errors", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerTxretries:                    newPromGaugeVec("kafka_broker_tx_retries", "Total number of request retries", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqTimeouts:                  newPromGaugeVec("kafka_broker_req_timeouts", "Total number of requests timed out", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerTxidle:                       newPromGaugeVec("kafka_broker_tx_idle", "Microseconds since last socket send (or -1 if no sends yet for current connection).", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRx:                           newPromGaugeVec("kafka_broker_rx", "Total number of responses received", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRxbytes:                      newPromGaugeVec("kafka_broker_rx_bytes", "Total number of bytes received", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRxerrs:                       newPromGaugeVec("kafka_broker_rx_errs", "Total number of receive errors", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRxcorriderrs:                 newPromGaugeVec("kafka_broker_rx_corrid_errs", "Total number of unmatched correlation ids in response (typically for timed out requests)", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRxpartial:                    newPromGaugeVec("kafka_broker_rx_partial", "Total number of partial MessageSets received. The broker may return partial responses if the full MessageSet could not fit in the remaining Fetch response size.", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRxIdle:                       newPromGaugeVec("kafka_broker_rx_idle", "Microseconds since last socket receive (or -1 if no receives yet for current connection).", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerZbufGrow:                     newPromGaugeVec("kafka_broker_zbuf_grow", "Total number of decompression buffer size increases", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerWakeups:                      newPromGaugeVec("kafka_broker_wakeups", "Broker thread poll loop wakeups", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerConnects:                     newPromGaugeVec("kafka_broker_connects", "Number of connection attempts, including successful and failed, and name resolution failures.", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerDisconnects:                  newPromGaugeVec("kafka_broker_disconnects", "Number of disconnects (triggered by broker, network, load-balancer, etc.).", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerIntLatencyMin:                newPromGaugeVec("kafka_broker_int_latency_min", "Internal producer queue latency in microseconds. Min value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerIntLatencyMax:                newPromGaugeVec("kafka_broker_int_latency_max", "Internal producer queue latency in microseconds. Max value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerIntLatencyAvg:                newPromGaugeVec("kafka_broker_int_latency_avg", "Internal producer queue latency in microseconds. Avg value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerIntLatency99:                 newPromGaugeVec("kafka_broker_int_latency_99", "Internal producer queue latency in microseconds. 99 Percentile value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerOutbufLatencyMin:             newPromGaugeVec("kafka_broker_outbuf_latency_min", "Internal request queue latency in microseconds. Min value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerOutbufLatencyMax:             newPromGaugeVec("kafka_broker_outbuf_latency_max", "Internal request queue latency in microseconds. Max value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerOutbufLatencyAvg:             newPromGaugeVec("kafka_broker_outbuf_latency_avg", "Internal request queue latency in microseconds. Avg value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerOutbufLatency99:              newPromGaugeVec("kafka_broker_outbuf_latency_99", "Internal request queue latency in microseconds. 99 Percentile value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRttMin:                       newPromGaugeVec("kafka_broker_rtt_min", "Broker latency / round-trip time in microseconds. Min value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRttMax:                       newPromGaugeVec("kafka_broker_rtt_max", "Broker latency / round-trip time in microseconds. Max value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRttAvg:                       newPromGaugeVec("kafka_broker_rtt_avg", "Broker latency / round-trip time in microseconds. Avg value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerRtt99:                        newPromGaugeVec("kafka_broker_rtt_99", "Broker latency / round-trip time in microseconds. 99 Percentile value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerThrottleMin:                  newPromGaugeVec("kafka_broker_throttle_min", "Broker throttling time in milliseconds. Min value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerThrottleMax:                  newPromGaugeVec("kafka_broker_throttle_max", "Broker throttling time in milliseconds. Max value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerThrottleAvg:                  newPromGaugeVec("kafka_broker_throttle_avg", "Broker throttling time in milliseconds. Avg value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerThrottle99:                   newPromGaugeVec("kafka_broker_throttle_99", "Broker throttling time in milliseconds. 99 Percentile value", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqProduce:                   newPromGaugeVec("kafka_broker_req_produce", "Broker Request Produce", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqListOffsets:               newPromGaugeVec("kafka_broker_req_listoffsets", "Broker Request List Offsets", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqMetadata:                  newPromGaugeVec("kafka_broker_req_metadata", "Broker Request Metadata", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqFindCoordinator:           newPromGaugeVec("kafka_broker_req_findcoordinator", "Broker Request Find Coordinator", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqSaslHandshake:             newPromGaugeVec("kafka_broker_req_saslhandshake", "Broker Request SaslHandshake", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqApiVersion:                newPromGaugeVec("kafka_broker_req_apiversion", "Broker Request Api Version", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqInitProducerId:            newPromGaugeVec("kafka_broker_req_initproducerid", "Broker Request Init ProducerId", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqAddPartitionsToTxn:        newPromGaugeVec("kafka_broker_req_addpartitionstotxns", "Broker Request Add Partitions To Txns", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqAddOffsetsToTxn:           newPromGaugeVec("kafka_broker_req_addoffsetstotxn", "Broker Request Add Offsets to Txn", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqEndTxn:                    newPromGaugeVec("kafka_broker_req_endtxn", "Broker Request End Txn", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqTxnOffsetCommit:           newPromGaugeVec("kafka_broker_req_txnoffsetcommit", "Broker Request Txn Offset Commit", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqSaslAuthenticate:          newPromGaugeVec("kafka_broker_req_saslauthenticate", "Broker Request Sasl Authenticate", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqDescribeCluster:           newPromGaugeVec("kafka_broker_req_describecluster", "Broker Request Describe Cluster", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqDescribeProducers:         newPromGaugeVec("kafka_broker_req_describeproducers", "Broker Request Describe Producers", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqDescribeTransactions:      newPromGaugeVec("kafka_broker_req_describetransactions", "Broker Request Describe Transactions", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqListTransactions:          newPromGaugeVec("kafka_broker_req_listtransactions", "Broker Request List Transactions", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqGetTelemetrySubscriptions: newPromGaugeVec("kafka_broker_req_telemetrysubscription", "Broker Request Telemetry Subscription", []string{"name", "client_id", "type", "broker", "state"}),
			BrokerReqPushTelemetry:             newPromGaugeVec("kafka_broker_req_pushtelemetry", "Broker Request Push Telemetry", []string{"name", "client_id", "type", "broker", "state"}),

			// Topic
			TopicMetadataAge:              newPromGaugeVec("kafka_topic_metadata_age", "Age of client's topic object (milliseconds)", []string{"name", "client_id", "type", "topic"}),
			TopicBatchsizeMin:             newPromGaugeVec("kafka_topic_batchsize_min", "Batch sizes in bytes. Min value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchsizeMax:             newPromGaugeVec("kafka_topic_batchsize_max", "Batch sizes in bytes. Max value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchsizeAvg:             newPromGaugeVec("kafka_topic_batchsize_avg", "Batch sizes in bytes. Avg value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchsize99:              newPromGaugeVec("kafka_topic_batchsize_99", "Batch sizes in bytes. 99 Percentile value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchcntMin:              newPromGaugeVec("kafka_topic_batchcnt_min", "Batch message counts. Min value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchcntMax:              newPromGaugeVec("kafka_topic_batchcnt_max", "Batch message counts. Max value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchcntAvg:              newPromGaugeVec("kafka_topic_batchcnt_avg", "Batch message counts. Avg value", []string{"name", "client_id", "type", "topic"}),
			TopicBatchcnt99:               newPromGaugeVec("kafka_topic_batchcnt_99", "Batch message counts 99 Percentile value", []string{"name", "client_id", "type", "topic"}),
			TopicPartitionBroker:          newPromGaugeVec("kafka_topic_partition_broker", "The id of the broker that messages are currently being fetched from", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionLeader:          newPromGaugeVec("kafka_topic_partition_leader", "Current leader broker id", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionMsgqCnt:         newPromGaugeVec("kafka_topic_partition_msgq_cnt", "Number of messages waiting to be produced in first-level queue", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionMsgqBytes:       newPromGaugeVec("kafka_topic_partition_msgq_bytes", "Number of bytes in msgq_cnt", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionXmitMsgqCnt:     newPromGaugeVec("kafka_topic_partition_xmit_msgq_cnt", "Number of messages ready to be produced in transmit queue", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionXmitMsgqBytes:   newPromGaugeVec("kafka_topic_partition_xmit_msgq_bytes", "Number of bytes in xmit_msgq", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionFetchqCnt:       newPromGaugeVec("kafka_topic_partition_fetchq_cnt", "Number of pre-fetched messages in fetch queue", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionFetchqSize:      newPromGaugeVec("kafka_topic_partition_fetchq_size", "Bytes in fetchq", []string{"name", "client_id", "type", "topic", "partition"}), // What is this
			TopicPartitionFetchState:      newPromGaugeVec("kafka_topic_partition_fetch_state", "Consumer fetch state for this partition (none, stopping, stopped, offset-query, offset-wait, active).", []string{"name", "client_id", "type", "topic", "partition", "state"}),
			TopicPartitionQueryOffset:     newPromGaugeVec("kafka_topic_partition_query_offset", "Current/Last logical offset query", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionNextOffset:      newPromGaugeVec("kafka_topic_partition_next_offset", "Next offset to fetch", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionAppOffset:       newPromGaugeVec("kafka_topic_partition_app_offset", "Offset of last message passed to application + 1", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionStoredOffset:    newPromGaugeVec("kafka_topic_partition_stored_offset", "Last committed offset", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionCommittedOffset: newPromGaugeVec("kafka_topic_partition_commited_offset", "Partition leader epoch of committed offset", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionEOFOffset:       newPromGaugeVec("kafka_topic_partition_eof_offset", "Last PARTITION_EOF signaled offset", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionLoOffset:        newPromGaugeVec("kafka_topic_partition_lo_offset", "Partition's low watermark offset on broker", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionHiOffset:        newPromGaugeVec("kafka_topic_partition_hi_offset", "Partition's high watermark offset on broker", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionConsumerLag:     newPromGaugeVec("kafka_topic_partition_consumer_lag", "Difference between (hi_offset or ls_offset) and committed_offset). hi_offset is used when isolation.level=read_uncommitted, otherwise ls_offset.", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionTxmsgs:          newPromGaugeVec("kafka_topic_partition_tx_msgs", "Total number of messages transmitted (produced)", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionTxbytes:         newPromGaugeVec("kafka_topic_partition_tx_bytes", "Total number of bytes transmitted for txmsgs", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionRxmsgs:          newPromGaugeVec("kafka_topic_partition_rx_msgs", "Total number of messages consumed, not including ignored messages (due to offset, etc).", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionRxbytes:         newPromGaugeVec("kafka_topic_partition_rx_bytes", "Total number of bytes received for rxmsgs", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionMsgs:            newPromGaugeVec("kafka_topic_partition_msgs", "Total number of messages received (consumer, same as rxmsgs), or total number of messages produced (possibly not yet transmitted) (producer).", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionRxVerDrops:      newPromGaugeVec("kafka_topic_partition_rx_ver_drops", "Dropped outdated messages", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionMsgsInflight:    newPromGaugeVec("kafka_topic_partition_msgs_in_flight", "	Current number of messages in-flight to/from broker", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionNextAckSeq:      newPromGaugeVec("kafka_topic_partition_next_ack_seq", "Next expected acked sequence (idempotent producer)", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionNextErrSeq:      newPromGaugeVec("kafka_topic_partition_next_err_seq", "Next expected errored sequence (idempotent producer)", []string{"name", "client_id", "type", "topic", "partition"}),
			TopicPartitionAckedMsgId:      newPromGaugeVec("kafka_topic_partition_acked_msgid", "Last acked internal message id (idempotent producer)", []string{"name", "client_id", "type", "topic", "partition"}),
		}
		prometheusStatistics.register()
	}
	return prometheusStatistics
}

func (p *PrometheusStatistics) register() {
	// Register Prometheus metrics
	prometheus.MustRegister(
		p.Time, p.Age, p.Replyq, p.MsgCnt, p.MsgMax,
		p.Tx, p.TxMsgs, p.TxMsgsBytes, p.Rx, p.RxMsgs, p.RxMsgsBytes,
		p.MetadataCacheCnt, p.Ts, p.MsgSize, p.TxBytes, p.RxBytes,
		p.CgrpState, p.CgrpJoinState, p.CgrpRebalanceAge, p.CgrpRebalanceCnt, p.CgrpRebalanceReason, p.CgrpAssignmentSize,
		p.EosIdempState, p.EosTxnState, p.EosTxnMayEnq, p.EosProducerEpoch, p.EosProducerID,

		// Broker
		p.BrokerOutbufCnt, p.BrokerOutbufMsgCnt, p.BrokerWaitrespCnt, p.BrokerWaitrespMsgCnt,
		p.BrokerTx, p.BrokerTxbytes, p.BrokerTxerrs, p.BrokerTxretries, p.BrokerTxidle, p.BrokerReqTimeouts,
		p.BrokerRx, p.BrokerRxbytes, p.BrokerRxerrs, p.BrokerRxcorriderrs, p.BrokerRxIdle, p.BrokerRxpartial,
		p.BrokerZbufGrow, p.BrokerWakeups, p.BrokerConnects, p.BrokerDisconnects,
		// Broker Window Stats
		p.BrokerIntLatencyMin, p.BrokerIntLatencyMax, p.BrokerIntLatencyAvg, p.BrokerIntLatency99,
		p.BrokerOutbufLatencyMin, p.BrokerOutbufLatencyMax, p.BrokerOutbufLatencyAvg, p.BrokerOutbufLatency99,
		p.BrokerRttMin, p.BrokerRttMax, p.BrokerRttAvg, p.BrokerRtt99,
		p.BrokerThrottleMin, p.BrokerThrottleMax, p.BrokerThrottleAvg, p.BrokerThrottle99,
		// Broker Requests
		p.BrokerReqProduce, p.BrokerReqListOffsets, p.BrokerReqMetadata, p.BrokerReqFindCoordinator,
		p.BrokerReqSaslHandshake, p.BrokerReqApiVersion, p.BrokerReqInitProducerId, p.BrokerReqAddPartitionsToTxn,
		p.BrokerReqAddOffsetsToTxn, p.BrokerReqEndTxn, p.BrokerReqTxnOffsetCommit, p.BrokerReqSaslAuthenticate,
		p.BrokerReqDescribeCluster, p.BrokerReqDescribeProducers, p.BrokerReqDescribeTransactions, p.BrokerReqListTransactions,
		p.BrokerReqGetTelemetrySubscriptions, p.BrokerReqPushTelemetry,

		// Topic
		p.TopicMetadataAge,
		p.TopicBatchsizeMin, p.TopicBatchsizeMax, p.TopicBatchsizeAvg, p.TopicBatchsize99,
		p.TopicBatchcntMin, p.TopicBatchcntMax, p.TopicBatchcntAvg, p.TopicBatchcnt99,
		p.TopicPartitionBroker, p.TopicPartitionLeader,
		p.TopicPartitionMsgqCnt, p.TopicPartitionMsgqBytes,
		p.TopicPartitionXmitMsgqCnt, p.TopicPartitionXmitMsgqBytes,
		p.TopicPartitionFetchqCnt, p.TopicPartitionFetchqSize, p.TopicPartitionFetchState,
		p.TopicPartitionQueryOffset, p.TopicPartitionNextOffset, p.TopicPartitionAppOffset, p.TopicPartitionStoredOffset, p.TopicPartitionCommittedOffset,
		p.TopicPartitionEOFOffset, p.TopicPartitionLoOffset, p.TopicPartitionHiOffset,
		p.TopicPartitionConsumerLag,
		p.TopicPartitionTxmsgs, p.TopicPartitionTxbytes,
		p.TopicPartitionRxmsgs, p.TopicPartitionRxbytes, p.TopicPartitionMsgs,
		p.TopicPartitionRxVerDrops,
		p.TopicPartitionMsgsInflight, p.TopicPartitionNextAckSeq, p.TopicPartitionNextErrSeq, p.TopicPartitionAckedMsgId,
	)

	// Serve Prometheus metrics
	go func() {
		http.Handle("/metrics", promhttp.Handler())
		infoLogger.Println("🚀 Prometheus metrics at http://:7021/metrics")
		http.ListenAndServe(":7021", nil)
	}()
}

func (p *PrometheusStatistics) HandleStats(stats string) {
	var kafkaStats KafkaStatistics
	if err := json.Unmarshal([]byte(stats), &kafkaStats); err != nil {
		errorLogger.Println("Failed to parse stats JSON:", err)
		return
	}
	infoLogger.Printf("Stats received for client type %s\n", kafkaStats.Type)

	p.populateClient(kafkaStats)

	for brokerName, broker := range kafkaStats.Brokers {
		p.populateBroker(brokerName, broker, kafkaStats)
	}

	for topicName, topic := range kafkaStats.Topics {
		p.populateTopic(topicName, topic, kafkaStats)
	}
}

func (p *PrometheusStatistics) populateClient(kafkaStats KafkaStatistics) {
	p.Time.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Time))
	p.Age.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Age))
	p.Replyq.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Replyq))
	p.MsgCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.MsgCnt))
	p.MsgMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.MsgMax))
	p.MetadataCacheCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.MetadataCacheCnt))
	p.Ts.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Ts))
	p.MsgSize.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.MsgSize))

	p.EosIdempState.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, kafkaStats.Eos.IdempState).Set(1)
	p.EosTxnState.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, kafkaStats.Eos.TxnState).Set(1)
	if kafkaStats.Eos.TxnMayEnq {
		p.EosTxnMayEnq.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(1))
	} else {
		p.EosTxnMayEnq.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(0))
	}
	p.EosProducerEpoch.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Eos.ProducerEpoch))
	p.EosProducerID.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Eos.ProducerID))

	p.Tx.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Tx))
	p.TxMsgs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.TxMsgs))
	p.TxMsgsBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.TxMsgsBytes))
	p.TxBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.TxBytes))
	p.Rx.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Rx))
	p.RxMsgs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.RxMsgs))
	p.RxMsgsBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.RxMsgsBytes))
	p.RxBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.RxBytes))

	p.CgrpState.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, kafkaStats.Cgrp.State).Set(1)
	p.CgrpJoinState.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, kafkaStats.Cgrp.JoinState).Set(1)
	p.CgrpRebalanceAge.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Cgrp.RebalanceAge))
	p.CgrpRebalanceCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Cgrp.RebalanceCnt))
	p.CgrpRebalanceReason.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, kafkaStats.Cgrp.RebalanceReason).Set(1)
	p.CgrpAssignmentSize.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type).Set(float64(kafkaStats.Cgrp.AssignmentSize))
}

func (p *PrometheusStatistics) populateBroker(brokerName string, broker Broker, kafkaStats KafkaStatistics) {
	if broker.State == "INIT" { // SKIP brokers that are not used to reduce metrics payload
		return
	}
	p.BrokerWaitrespCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.WaitrespCnt))
	p.BrokerWaitrespMsgCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.WaitrespMsgCnt))
	p.BrokerTx.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Tx))
	p.BrokerTxbytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Txbytes))
	p.BrokerTxerrs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Txerrs))
	p.BrokerTxretries.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Txretries))
	p.BrokerTxidle.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Txidle))
	p.BrokerReqTimeouts.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.ReqTimeouts))
	p.BrokerRx.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rx))
	p.BrokerRxbytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rxbytes))
	p.BrokerRxerrs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rxerrs))
	p.BrokerRxcorriderrs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rxcorriderrs))
	p.BrokerRxIdle.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rxidle))
	p.BrokerRxpartial.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rxpartial))
	p.BrokerZbufGrow.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.ZbufGrow))
	p.BrokerWakeups.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Wakeups))
	p.BrokerConnects.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Connects))
	p.BrokerDisconnects.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Disconnects))

	p.BrokerOutbufCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufCnt))
	p.BrokerOutbufMsgCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufMsgCnt))
	p.BrokerOutbufLatencyMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufLatency.Min))
	p.BrokerOutbufLatencyMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufLatency.Max))
	p.BrokerOutbufLatencyAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufLatency.Avg))
	p.BrokerOutbufLatency99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.OutbufLatency.P99))

	p.BrokerIntLatencyMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.IntLatency.Min))
	p.BrokerIntLatencyMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.IntLatency.Max))
	p.BrokerIntLatencyAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.IntLatency.Avg))
	p.BrokerIntLatency99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.IntLatency.P99))

	p.BrokerRttMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rtt.Min))
	p.BrokerRttMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rtt.Max))
	p.BrokerRttAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rtt.Avg))
	p.BrokerRtt99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Rtt.P99))
	p.BrokerThrottleMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Throttle.Min))
	p.BrokerThrottleMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Throttle.Max))
	p.BrokerThrottleAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Throttle.Avg))
	p.BrokerThrottle99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Throttle.P99))

	p.BrokerReqProduce.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.Produce))
	p.BrokerReqListOffsets.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.ListOffsets))
	p.BrokerReqMetadata.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.Metadata))
	p.BrokerReqFindCoordinator.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.FindCoordinator))
	p.BrokerReqSaslHandshake.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.SaslHandshake))
	p.BrokerReqApiVersion.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.ApiVersion))
	p.BrokerReqInitProducerId.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.InitProducerId))
	p.BrokerReqAddPartitionsToTxn.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.AddPartitionsToTxn))
	p.BrokerReqAddOffsetsToTxn.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.AddOffsetsToTxn))
	p.BrokerReqEndTxn.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.EndTxn))
	p.BrokerReqTxnOffsetCommit.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.TxnOffsetCommit))
	p.BrokerReqSaslAuthenticate.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.SaslAuthenticate))
	p.BrokerReqDescribeCluster.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.DescribeCluster))
	p.BrokerReqDescribeProducers.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.DescribeProducers))
	p.BrokerReqDescribeTransactions.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.DescribeTransactions))
	p.BrokerReqListTransactions.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.GetTelemetrySubscriptions))
	p.BrokerReqGetTelemetrySubscriptions.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.GetTelemetrySubscriptions))
	p.BrokerReqPushTelemetry.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, brokerName, broker.State).Set(float64(broker.Req.PushTelemetry))
}

func (p *PrometheusStatistics) populateTopic(topicName string, topic Topic, kafkaStats KafkaStatistics) {
	p.TopicMetadataAge.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.MetadataAge))
	p.TopicBatchsizeMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchsize.Min))
	p.TopicBatchsizeMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchsize.Max))
	p.TopicBatchsizeAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchsize.Avg))
	p.TopicBatchsize99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchsize.P99))
	p.TopicBatchcntMin.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchcnt.Min))
	p.TopicBatchcntMax.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchcnt.Max))
	p.TopicBatchcntAvg.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchcnt.Avg))
	p.TopicBatchcnt99.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName).Set(float64(topic.Batchcnt.P99))

	for partStr, part := range topic.Partitions {
		p.populatePartition(partStr, part, topicName, kafkaStats)
	}
}

func (p *PrometheusStatistics) populatePartition(partStr string, part Partition, topicName string, kafkaStats KafkaStatistics) {
	p.TopicPartitionBroker.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Broker))
	p.TopicPartitionLeader.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Leader))
	p.TopicPartitionMsgqCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.MsgqCnt))
	p.TopicPartitionMsgqBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.MsgqBytes))
	p.TopicPartitionXmitMsgqCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.XmitMsgqCnt))
	p.TopicPartitionXmitMsgqBytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.XmitMsgqBytes))
	p.TopicPartitionFetchqCnt.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.FetchqCnt))
	p.TopicPartitionFetchqSize.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.FetchqSize))
	p.TopicPartitionFetchState.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr, part.FetchState).Set(1)
	p.TopicPartitionQueryOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.QueryOffset))
	p.TopicPartitionNextOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.NextOffset))
	p.TopicPartitionAppOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.AppOffset))
	p.TopicPartitionStoredOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.StoredOffset))
	p.TopicPartitionCommittedOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.CommitedOffset))
	p.TopicPartitionEOFOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.EOFOffset))
	p.TopicPartitionLoOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.LoOffset))
	p.TopicPartitionHiOffset.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.HiOffset))
	p.TopicPartitionConsumerLag.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.ConsumerLag))
	p.TopicPartitionTxmsgs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Txmsgs))
	p.TopicPartitionTxbytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Txbytes))
	p.TopicPartitionRxmsgs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Rxmsgs))
	p.TopicPartitionRxbytes.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Rxbytes))
	p.TopicPartitionMsgs.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.Msgs))
	p.TopicPartitionRxVerDrops.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.RxVerDrops))
	p.TopicPartitionMsgsInflight.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.MsgsInflight))
	p.TopicPartitionNextAckSeq.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.NextAckSeq))
	p.TopicPartitionNextErrSeq.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.NextErrSeq))
	p.TopicPartitionAckedMsgId.WithLabelValues(kafkaStats.Name, kafkaStats.ClientID, kafkaStats.Type, topicName, partStr).Set(float64(part.AckedMsgId))
}
