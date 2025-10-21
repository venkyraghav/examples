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

type KafkaStatistics struct {
	Name             string `json:"name"`
	ClientID         string `json:"client_id"`
	Type             string `json:"type"`
	Ts               int64  `json:"ts"`
	Time             int    `json:"time"`
	Replyq           int    `json:"replyq"`
	MsgCnt           int    `json:"msg_cnt"`
	MsgSize          int    `json:"msg_size"`
	MsgMax           int    `json:"msg_max"`
	MsgSizeMax       int    `json:"msg_size_max"`
	SimpleCnt        int    `json:"simple_cnt"`
	MetadataCacheCnt int    `json:"metadata_cache_cnt"`
	Brokers          map[string]struct {
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
		ReqTimeouts    int              `json:"req_timeouts"`
		Rx             int              `json:"rx"`
		Rxbytes        int              `json:"rxbytes"`
		Rxerrs         int              `json:"rxerrs"`
		Rxcorriderrs   int              `json:"rxcorriderrs"`
		Rxpartial      int              `json:"rxpartial"`
		ZbufGrow       int              `json:"zbuf_grow"`
		BufGrow        int              `json:"buf_grow"`
		Wakeups        int              `json:"wakeups"`
		IntLatency     WindowStatistics `json:"int_latency"`
		Rtt            WindowStatistics `json:"rtt"`
		Throttle       WindowStatistics `json:"throttle"`
		Toppars        map[string]struct {
			Topic     string `json:"topic"`
			Partition int    `json:"partition"`
		} `json:"toppars"`
	} `json:"brokers"`
	Topics map[string]struct {
		TopicName   string           `json:"topic"`
		MsgCnt      int              `json:"msg_cnt"`
		MetadataAge int              `json:"metadata_age"`
		Batchsize   WindowStatistics `json:"batchsize"`
		Batchcnt    WindowStatistics `json:"batchcnt"`
		Partitions  map[string]struct {
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
		} `json:"partitions"`
	} `json:"topics"`
	Tx         int `json:"tx"`
	TxBytes    int `json:"tx_bytes"`
	Rx         int `json:"rx"`
	RxBytes    int `json:"rx_bytes"`
	Txmsgs     int `json:"txmsgs"`
	TxmsgBytes int `json:"txmsg_bytes"`
	Rxmsgs     int `json:"rxmsgs"`
	RxmsgBytes int `json:"rxmsg_bytes"`
}

// Remaining metrics
// Name             string `json:"name"`
// ClientID         string `json:"client_id"`
// Type             string `json:"type"`
// Ts               int64  `json:"ts"`
// Time             int    `json:"time"`
// Replyq           int    `json:"replyq"`
// MsgSize          int    `json:"msg_size"`
// MsgMax           int    `json:"msg_max"`
// MsgSizeMax       int    `json:"msg_size_max"`
// SimpleCnt        int    `json:"simple_cnt"`
// MetadataCacheCnt int    `json:"metadata_cache_cnt"`
// Brokers
// Topics
// Tx         int `json:"tx"`
// TxBytes    int `json:"tx_bytes"`
// Rx         int `json:"rx"`
// RxBytes    int `json:"rx_bytes"`
// TxmsgBytes int `json:"txmsg_bytes"`
// RxmsgBytes int `json:"rxmsg_bytes"`
type PrometheusStatistics struct {
	TxMsgs prometheus.Gauge
	RxMsgs prometheus.Gauge
	MsgCnt prometheus.Gauge
}

var prometheusStatistics *PrometheusStatistics

func GetPrometheusStatistics() *PrometheusStatistics {
	if prometheusStatistics == nil {
		prometheusStatistics = &PrometheusStatistics{
			TxMsgs: prometheus.NewGauge(prometheus.GaugeOpts{
				Name: "kafka_tx_msgs_total",
				Help: "Total messages sent",
			}),
			RxMsgs: prometheus.NewGauge(prometheus.GaugeOpts{
				Name: "kafka_rx_msgs_total",
				Help: "Total messages received",
			}),
			MsgCnt: prometheus.NewGauge(prometheus.GaugeOpts{
				Name: "kafka_msg_cnt",
				Help: "Messages currently queued in producer",
			}),
		}
		prometheusStatistics.register()
	}
	return prometheusStatistics
}

func (p *PrometheusStatistics) register() {
	// Register Prometheus metrics
	prometheus.MustRegister(
		p.TxMsgs,
		p.RxMsgs,
		p.MsgCnt,
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
	infoLogger.Printf("Stats received \n")
	if err := json.Unmarshal([]byte(stats), &kafkaStats); err != nil {
		errorLogger.Println("Failed to parse stats JSON:", err)
		return
	}
	infoLogger.Printf("Stats struct %v\n", kafkaStats)

	// Update Prometheus metrics
	p.TxMsgs.Set(float64(kafkaStats.Txmsgs))
	p.RxMsgs.Set(float64(kafkaStats.Rxmsgs))
	p.MsgCnt.Set(float64(kafkaStats.MsgCnt))

	// Optional: Trigger alerting logic
	p.checkForAlerts(kafkaStats)
}

func (p *PrometheusStatistics) checkForAlerts(stats KafkaStatistics) {
	if stats.Txmsgs == 0 {
		infoLogger.Println("🚨 ALERT: No messages being sent!")
	}
	if stats.MsgCnt > 10000 {
		infoLogger.Println("🚨 ALERT: Producer queue is backing up!")
	}
}
