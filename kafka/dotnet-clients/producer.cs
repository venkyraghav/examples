using Confluent.Kafka.Admin;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Confluent.SchemaRegistry.Serdes;
using System.Text;
using System.Net.Http;sd
using System.CommandLine;

namespace Confluent.Kafka.Examples
{
    public class User
    {
        [JsonProperty(PropertyName = "favoriteColor")]
        public String favorite_color;

        [JsonProperty(PropertyName = "favoriteNumber")]
        public int favorite_number;

        [JsonProperty(PropertyName = "hourlyRate")]
        public Decimal hourly_rate;

        [JsonProperty(PropertyName = "name")]
        public String name;
    }

    public class NewRelicMetricPayload
    {
        public const int STATS_TIME = 10000;

        [JsonProperty(PropertyName = "common")]
        public NewRelicCommon Common { get; set; }

        [JsonProperty(PropertyName = "metrics")]
        public List<NewRelicMetrics> Metrics { get; set; }

    }

    public class NewRelicMetrics
    {
        [JsonProperty(PropertyName = "name")]
        public string Name { get; set; }

        [JsonProperty(PropertyName = "type")]
        public string Type { get; set; }

        [JsonProperty(PropertyName = "value")]
        public dynamic Value { get; set; }

        [JsonProperty(PropertyName = "timestamp")]
        public long Timestamp { get; set; }
    }

    public class NewRelicCommon
    {
        [JsonProperty(PropertyName = "attributes")]
        public Dictionary<string, string> Attributes { get; set; }

        [JsonProperty(PropertyName = "interval.ms")]
        public long IntervalMs { get; set; }

        [JsonProperty(PropertyName = "timestamp")]
        public long Timestamp { get; set; }
    }

    public class ClientStatistics
    {
        [JsonProperty(PropertyName = "name")]
        public string Name { get; set; }

        [JsonProperty(PropertyName = "client_id")]
        public string ClientId { get; set; }

        [JsonProperty(PropertyName = "time")]
        public long Time { get; set; }

        [JsonProperty(PropertyName = "msg_cnt")]
        public long MsgCnt { get; set; }

        [JsonProperty(PropertyName = "tx")]
        public long Tx { get; set; }

        [JsonProperty(PropertyName = "txmsgs")]
        public long TxMsgs { get; set; }

        [JsonProperty(PropertyName = "brokers")]
        public Dictionary<string, BrokerStatistics> Brokers { get; set; }

        [JsonProperty(PropertyName = "topics")]
        public Dictionary<string, TopicStatistics> Topics { get; set; }

        [JsonProperty(PropertyName = "cgrp")]
        public ConsumerGroupStatistics Cgrp { get; set; }
    }

    public class ConsumerGroupStatistics
    {
        [JsonProperty(PropertyName = "state")]
        public string State { get; set; }

        [JsonProperty(PropertyName = "rebalance_age")]
        public long RebalanceAge { get; set; }

        [JsonProperty(PropertyName = "rebalance_cnt")]
        public long RebalanceCnt { get; set; }

        [JsonProperty(PropertyName = "rebalance_reason")]
        public string RebalanceReason { get; set; }

    }

    public class BrokerStatistics
    {
        [JsonProperty(PropertyName = "nodename")]
        public string NodeName { get; set; }

        [JsonProperty(PropertyName = "state")]
        public string State { get; set; }

        [JsonProperty(PropertyName = "outbuf_msg_cnt")]
        public long OutBufMsgCnt { get; set; }

        [JsonProperty(PropertyName = "tx")]
        public long Tx { get; set; }

        [JsonProperty(PropertyName = "txerrs")]
        public long TxErrs { get; set; }

        [JsonProperty(PropertyName = "txretries")]
        public long TxRetries { get; set; }

        [JsonProperty(PropertyName = "req_timeouts")]
        public long ReqTimeouts { get; set; }

        [JsonProperty(PropertyName = "rx")]
        public long Rx { get; set; }

        [JsonProperty(PropertyName = "rxerrs")]
        public long RxErrs { get; set; }

        [JsonProperty(PropertyName = "connects")]
        public long Connects { get; set; }

        [JsonProperty(PropertyName = "disconnects")]
        public long Disconnects { get; set; }

        [JsonProperty(PropertyName = "rtt")]
        public WindowStatistics Rtt { get; set; }

        [JsonProperty(PropertyName = "throttle")]
        public WindowStatistics Throttle { get; set; }

        [JsonProperty(PropertyName = "outbuf_latency")]
        public WindowStatistics OutbufLatency { get; set; }

        [JsonProperty(PropertyName = "int_latency")]
        public WindowStatistics IntLatency { get; set; }

        [JsonProperty(PropertyName = "req")]
        public RequestStatitics Request { get; set; }

        [JsonProperty(PropertyName = "toppars")]
        public TopPars TopPartitions { get; set; }
    }

    public class RequestStatitics
    {
        [JsonProperty(PropertyName = "Produce")]
        public long Produce { get; set; }

        [JsonProperty(PropertyName = "Fetch")]
        public long Fetch { get; set; }

        [JsonProperty(PropertyName = "SaslAuthenticate")]
        public long SaslAuthenticate { get; set; }

        [JsonProperty(PropertyName = "JoinGroup")]
        public long JoinGroup { get; set; }

        [JsonProperty(PropertyName = "Heartbeat")]
        public long Heartbeat { get; set; }

        [JsonProperty(PropertyName = "LeaveGroup")]
        public long LeaveGroup { get; set; }

        [JsonProperty(PropertyName = "SyncGroup")]
        public long SyncGroup { get; set; }
    }

    public class TopPars
    {
        [JsonProperty(PropertyName = "topic")]
        public string Topic { get; set; }

        [JsonProperty(PropertyName = "partition")]
        public long Partition { get; set; }
    }

    public class WindowStatistics
    {
        [JsonProperty(PropertyName = "min")]
        public long Min { get; set; }

        [JsonProperty(PropertyName = "max")]
        public long Max { get; set; }

        [JsonProperty(PropertyName = "avg")]
        public long Avg { get; set; }

        [JsonProperty(PropertyName = "cnt")]
        public long Cnt { get; set; }

        [JsonProperty(PropertyName = "p99")]
        public long P99 { get; set; }
    }

    public class PartitionStatistics
    {
        [JsonProperty(PropertyName = "partition")]
        public long partition { get; set; }

        [JsonProperty(PropertyName = "broker")]
        public long Broker { get; set; }

        [JsonProperty(PropertyName = "leader")]
        public long Leader { get; set; }

        [JsonProperty(PropertyName = "txmsgs")]
        public long Txmsgs { get; set; }

        [JsonProperty(PropertyName = "p99")]
        public long P99 { get; set; }

        [JsonProperty(PropertyName = "consumer_lag")]
        public long ConsumerLag { get; set; }

        [JsonProperty(PropertyName = "committed_offset")]
        public long CommittedOffset { get; set; }

        [JsonProperty(PropertyName = "stored_offset")]
        public long StoredOffset { get; set; }

        [JsonProperty(PropertyName = "hi_offset")]
        public long HiOffset { get; set; }

        [JsonProperty(PropertyName = "next_offset")]
        public long NextOffset { get; set; }

        [JsonProperty(PropertyName = "app_offset")]
        public long AppOffset { get; set; }
    }
    public class TopicStatistics
    {
        [JsonProperty(PropertyName = "topic")]
        public string Topic { get; set; }

        [JsonProperty(PropertyName = "batchsize")]
        public WindowStatistics BatchSize { get; set; }

        [JsonProperty(PropertyName = "batchcnt")]
        public WindowStatistics BatchCnt { get; set; }

        [JsonProperty(PropertyName = "partitions")]
        public Dictionary<string, PartitionStatistics> Partitions { get; set; }
    }

    class Producer
    {
        static async Task<Dictionary<string, string>> LoadConfig(string configPath, string certDir)
        {
            try
            {
                var cloudConfig = (await File.ReadAllLinesAsync(configPath))
                    .Where(line => !line.StartsWith("#") && !string.IsNullOrEmpty(line))
                    .ToDictionary(
                        line => line.Substring(0, line.IndexOf('=')),
                        line => line.Substring(line.IndexOf('=') + 1));

                return cloudConfig;
            }
            catch (Exception e)
            {
                Console.WriteLine($"An error occurred reading the config file from '{configPath}': {e.Message}");
                System.Environment.Exit(1);
                return null; // avoid not-all-paths-return-value compiler error.
            }
        }

        // static async CachedSchemaRegistryClient CreateSchemaRegistryClient(Dictionary<string, string> clientConfig)
        // {
        //     SchemaRegistryConfig config = new SchemaRegistryConfig();

        //     if (clientConfig.ContainsKey("sasl.username")) {
        //         config.BasicAuthCredentialsSource = AuthCredentialsSource.UserInfo;
        //         config.BasicAuthUserInfo = clientConfig["sasl.username"] + ":" + clientConfig["sasl.password"];
        //     }

        //     config.Url = clientConfig["schema.registry.url"];
        //     if (clientConfig.ContainsKey ("ssl.ca.location")) {
        //         config.SslCaLocation = clientConfig["ssl.ca.location"];
        //     }
        //     // TODO Bad practice. Very Bad. For some reason, C# code is rejecting my crt file while I curl provides good response.
        //     // curl --cacert /Users/vnarayanan/_official/vagrant/data/kafka-truststore.crt -X GET -u venky-client2:Kafka4Ever https://sr01.venky.net.compute.internal:8081/subjects
        //     // FIXME Should be config.EnableSslCertificateVerification = true
        //     config.EnableSslCertificateVerification = false;

        //     return Task.FromResult(new CachedSchemaRegistryClient(config));
        // }

        static async Task<ClientConfig> CreateCommonConfig(Dictionary<string, string> clientConfig)
        {
            ClientConfig config = new ClientConfig();

            if (clientConfig.ContainsKey ("bootstrap.servers")) {
              config.BootstrapServers = clientConfig["bootstrap.servers"];
            }
            if (clientConfig.ContainsKey ("client.id")) {
              config.ClientId = clientConfig["client.id"];
            }
            if (clientConfig.ContainsKey ("client.rack")) {
              config.ClientRack =  clientConfig["client.rack"];
            }

            if (clientConfig.ContainsKey ("sasl.mechanisms")) {
                if (string.Equals(clientConfig["sasl.mechanisms"], "PLAIN")) {
                config.SaslMechanism = SaslMechanism.Plain;
                } else {
                    Console.WriteLine($"Unsupported SaslMechanism '{clientConfig["sasl.mechanisms"]}'");
                    System.Environment.Exit(1);
                    return null;
                }
            }
            if (clientConfig.ContainsKey ("security.protocol")) {
                if (string.Equals(clientConfig["security.protocol"], "SASL_SSL")) {
                    config.SecurityProtocol = SecurityProtocol.SaslSsl;
                    if (clientConfig.ContainsKey ("ssl.ca.location")) {
                    config.SslCaLocation = clientConfig["ssl.ca.location"];
                    }
                    config.SslEndpointIdentificationAlgorithm = SslEndpointIdentificationAlgorithm.Https;
                    config.SaslUsername = clientConfig["sasl.username"];
                    config.SaslPassword = clientConfig["sasl.password"];
                } else {
                    Console.WriteLine($"Unsupported SecurityProtocol '{clientConfig["security.protocol"]}'");
                    System.Environment.Exit(1);
                    return null;
                }
            }

            config.StatisticsIntervalMs = NewRelicMetricPayload.STATS_TIME;

            return config;
        }

        static async Task<ClientConfig> CreateAdminConfig(Dictionary<string, string> clientConfig)
        {
            return await CreateCommonConfig(clientConfig);
        }

        static async Task<ClientConfig> CreateProducerConfig(Dictionary<string, string> clientConfig)
        {
            return await CreateCommonConfig(clientConfig);
        }

        static async Task<ConsumerConfig> CreateConsumerConfig(Dictionary<string, string> clientConfig)
        {
            ClientConfig c = await CreateCommonConfig(clientConfig);
            ConsumerConfig config = new ConsumerConfig(c);
            config.GroupId = clientConfig["group.id"];
            config.AutoOffsetReset = AutoOffsetReset.Earliest; // TODO Get this from config
            // config.EnableAutoCommit = false; // TODO Get this from config
            config.FetchMaxBytes = 1024*1024 + 2;

            return config;
        }

        static async Task CreateTopicMaybe(string name, int numPartitions, short replicationFactor, Dictionary<string, string> config)
        {
            using (var adminClient = new AdminClientBuilder(await CreateAdminConfig(config)).Build())
            {
                try
                {
                    await adminClient.CreateTopicsAsync(new List<TopicSpecification> {
                        new TopicSpecification { Name = name, NumPartitions = numPartitions, ReplicationFactor = replicationFactor }
                        });
                }
                catch (CreateTopicsException e)
                {
                    if (e.Results[0].Error.Code != ErrorCode.TopicAlreadyExists)
                    {
                        Console.WriteLine($"An error occured creating topic {name}: {e.Results[0].Error.Reason}");
                    }
                    else
                    {
                        Console.WriteLine("Topic already exists");
                    }
                }
            }
        }

        private static HttpClient sharedClient = new()
        {
            BaseAddress = new Uri("https://metric-api.newrelic.com/"),
        };

        enum BrokerState {INIT = 0, DOWN = 1, CONNECT = 2, AUTH = 3, APIVERSION_QUERY = 4, AUTH_HANDSHAKE = 5, UP = 6, UPDATE = 7};

        static async Task PublishMetrics(string appName, string clientType, string json) {
            Console.WriteLine("Stats = " + Newtonsoft.Json.JsonConvert.SerializeObject(json, Formatting.None));

            var stats = JsonConvert.DeserializeObject<ClientStatistics>(json);
            var prefix = clientType + ".";

            NewRelicMetricPayload payload = new NewRelicMetricPayload();
            payload.Common = new NewRelicCommon();
            payload.Common.IntervalMs = NewRelicMetricPayload.STATS_TIME;
            payload.Common.Timestamp = stats.Time;

            var metricsPrefix = prefix;
            payload.Common.Attributes = new Dictionary<string, string>();
            payload.Common.Attributes.Add("app.name", appName);
            payload.Common.Attributes.Add("app.clientid", stats.ClientId);

            payload.Metrics = new List<NewRelicMetrics>();
            payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "Tx", stats.Tx, "cumulativeCount", stats.Time));
            payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "TxMsgs", stats.TxMsgs, "cumulativeCount", stats.Time));
            foreach (var broker in stats.Brokers)
            {
                //
                metricsPrefix = prefix + "brokers." + broker.Value.NodeName + ".";
                BrokerState brokerState = 0;
                if (broker.Value.State.Equals("UP")) {
                    brokerState = BrokerState.UP;
                } else if (broker.Value.State.Equals("INIT")) {
                    brokerState = BrokerState.INIT;
                } else if (broker.Value.State.Equals("DOWN")) {
                    brokerState = BrokerState.DOWN;
                } else if (broker.Value.State.Equals("CONNECT")) {
                    brokerState = BrokerState.CONNECT;
                } else if (broker.Value.State.Equals("AUTH")) {
                    brokerState = BrokerState.AUTH;
                } else if (broker.Value.State.Equals("APIVERSION_QUERY")) {
                    brokerState = BrokerState.APIVERSION_QUERY;
                } else if (broker.Value.State.Equals("AUTH_HANDSHAKE")) {
                    brokerState = BrokerState.AUTH_HANDSHAKE;
                } else if (broker.Value.State.Equals("UPDATE")) {
                    brokerState = BrokerState.UPDATE;
                }
                payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "broker." + broker.Value.NodeName + ".state", brokerState , "gauge", stats.Time));
                if (broker.Value.OutBufMsgCnt >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "outbuf_msg_cnt", broker.Value.OutBufMsgCnt , "cumulativeCount", stats.Time));
                }
                if (broker.Value.Tx >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "tx", broker.Value.Tx , "cumulativeCount", stats.Time));
                }
                if (broker.Value.TxErrs >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix  + "txerrs", broker.Value.TxErrs , "cumulativeCount", stats.Time));
                }
                if (broker.Value.TxRetries >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "txretries", broker.Value.TxRetries , "cumulativeCount", stats.Time));
                }
                if (broker.Value.ReqTimeouts >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "req_timeouts", broker.Value.ReqTimeouts , "cumulativeCount", stats.Time));
                }
                if (broker.Value.Rx >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "rx", broker.Value.Rx , "cumulativeCount", stats.Time));
                }
                if (broker.Value.RxErrs >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "rxerrs", broker.Value.RxErrs , "cumulativeCount", stats.Time));
                }
                if (broker.Value.Connects >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "connects", broker.Value.Connects , "cumulativeCount", stats.Time));
                }
                if (broker.Value.Disconnects >= 0) {
                    payload.Metrics.Add(GetNewRelicMetricsItem(metricsPrefix + "disconnects", broker.Value.Disconnects , "cumulativeCount", stats.Time));
                }
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "rtt", broker.Value.Rtt, stats.Time);
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "throttle", broker.Value.Throttle, stats.Time);
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "outbuf_latency", broker.Value.OutbufLatency, stats.Time);
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "int_latency", broker.Value.IntLatency, stats.Time);

                PopulateNewRelicMetricsRequestStatistics(payload.Metrics, metricsPrefix + "req", broker.Value.Request, stats.Time);
            }
            foreach (var topic in stats.Topics)
            {
                metricsPrefix = prefix + "topics." + topic.Value.Topic + ".";
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "batchsize", topic.Value.BatchSize, stats.Time);
                PopulateNewRelicMetricsWindowStatistics(payload.Metrics, metricsPrefix + "batchcnt", topic.Value.BatchCnt, stats.Time);
                foreach (var partition in topic.Value.Partitions)
                {
                    metricsPrefix = prefix + "topics." + topic.Value.Topic + ".partition." + partition.Value.partition + ".";
                    if (partition.Key.Equals("-1") == false) {
                        PopulateNewRelicMetricsPartitionStatistics(payload.Metrics, metricsPrefix, partition.Value, stats.Time);
                    }
                }
            }
            metricsPrefix = prefix + "cgrp.";
            PopulateNewRelicMetricsConsumerGroupStatistics(payload.Metrics, metricsPrefix, stats.Cgrp, stats.Time);

            Console.WriteLine("Stats Modified = " + Newtonsoft.Json.JsonConvert.SerializeObject(payload, Formatting.None));

            using StringContent jsonContent = new("[" + Newtonsoft.Json.JsonConvert.SerializeObject(payload, Formatting.None) + "]", Encoding.UTF8, "application/json");
            sharedClient.DefaultRequestHeaders.Accept.Clear();
            sharedClient.DefaultRequestHeaders.Clear();
            sharedClient.DefaultRequestHeaders.Add("X-License-Key", "7ab2d90c05c28c9ff4785878ef356683ae808005");
            try
            {
                using HttpResponseMessage response = await sharedClient.PostAsync("metric/v1", jsonContent);
                if (response.IsSuccessStatusCode) {
                    var data = await response.Content.ReadAsStringAsync();
                    Console.WriteLine("Posted to NewRelic " + data);
                } else {
                    Console.WriteLine("Failed Posting to NewRelic code " + response.StatusCode + " " + response.ReasonPhrase);
                }
            }
            catch (System.Exception e)
            {
                Console.Error.WriteLine("Error Posting to NewRelic", e);
                Console.Error.WriteLine(e.StackTrace.ToString());
            }
        }

        enum CgrpState {UNKNOWN = 0, UP = 1}
        private static void PopulateNewRelicMetricsConsumerGroupStatistics(List<NewRelicMetrics> mainList, string statsName, ConsumerGroupStatistics cgrp, long timestamp)
        {
            if (cgrp == null) {
                return;
            }
            if (cgrp.State.Equals("up")) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "state", CgrpState.UP, "gauge", timestamp));
            } else {
                mainList.Add(GetNewRelicMetricsItem(statsName + "state", CgrpState.UNKNOWN, "gauge", timestamp));
            }
            mainList.Add(GetNewRelicMetricsItem(statsName + "rebalance_age", cgrp.RebalanceAge, "gauge", timestamp));
            mainList.Add(GetNewRelicMetricsItem(statsName + "rebalance_cnt", cgrp.RebalanceCnt, "gauge", timestamp));
            Console.WriteLine("Consumer Group Rebalance Reason " + cgrp.RebalanceReason);
        }

        private static void PopulateNewRelicMetricsPartitionStatistics(List<NewRelicMetrics> mainList, string statsName, PartitionStatistics partition, long timestamp)
        {
            if (partition.Broker >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "broker", partition.Broker, "gauge", timestamp));
            }
            if (partition.Leader >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "leader", partition.Leader, "gauge", timestamp));
            }
            if (partition.Txmsgs >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "txmsgs", partition.Txmsgs, "cumulativeCount", timestamp));
            }
            if (partition.P99 >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "p99", partition.P99, "gauge", timestamp));
            }
            if (partition.ConsumerLag >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "consumer_lag", partition.ConsumerLag, "gauge", timestamp));
            }
            if (partition.CommittedOffset >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "committed_offset", partition.CommittedOffset, "gauge", timestamp));
            }
            if (partition.StoredOffset >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "stored_offset", partition.StoredOffset, "gauge", timestamp));
            }
            if (partition.HiOffset >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "hi_offset", partition.HiOffset, "gauge", timestamp));
            }
            if (partition.NextOffset >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "next_offset", partition.NextOffset, "gauge", timestamp));
            }
            if (partition.AppOffset >= 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + "app_offset", partition.AppOffset, "gauge", timestamp));
            }
        }

        private static void PopulateNewRelicMetricsRequestStatistics(List<NewRelicMetrics> mainList, string statsName, RequestStatitics item, long timestamp)
        {
            NewRelicMetrics cnt = GetNewRelicMetricsItem(statsName + ".Produce", item.Produce, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".Fetch", item.Fetch, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".SaslAuthenticate", item.SaslAuthenticate, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".JoinGroup", item.JoinGroup, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".Heartbeat", item.Heartbeat, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".LeaveGroup", item.LeaveGroup, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
            cnt = GetNewRelicMetricsItem(statsName + ".SyncGroup", item.SyncGroup, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(cnt);
            }
        }

        private static void PopulateNewRelicMetricsWindowStatistics(List<NewRelicMetrics> mainList, string statsName, WindowStatistics item, long timestamp)
        {
            NewRelicMetrics cnt = GetNewRelicMetricsItem(statsName + ".cnt", item.Cnt, "gauge", timestamp);
            if (cnt.Value != 0) {
                mainList.Add(GetNewRelicMetricsItem(statsName + ".avg", item.Avg, "gauge", timestamp));
                mainList.Add(GetNewRelicMetricsItem(statsName + ".min", item.Min, "gauge", timestamp));
                mainList.Add(GetNewRelicMetricsItem(statsName + ".max", item.Max, "gauge", timestamp));
                mainList.Add(GetNewRelicMetricsItem(statsName + ".cnt", item.Cnt, "gauge", timestamp));
                mainList.Add(GetNewRelicMetricsItem(statsName + ".p99", item.P99, "gauge", timestamp));
                mainList.Add(cnt);
            }

        }

        private static NewRelicMetrics GetNewRelicMetricsItem(string name, dynamic value, string type, long timestamp)
        {
            NewRelicMetrics item = new NewRelicMetrics();
            item.Name = name;
            item.Value = value;
            item.Type = type;
            item.Timestamp = timestamp;
            return item;
        }

        static async Task Produce(string topic, Dictionary<string, string> config)
        {
            string appName = "Confluent_DotNet_Test";

            var avroSerializerConfig = new AvroSerializerConfig
            {
                BufferBytes = 100
            };
            // var schemaRegistry = await CreateSchemaRegistryClient(config);
            using (var producer = new ProducerBuilder<string, string>(await CreateProducerConfig(config))
                //.SetKeySerializer(new AvroSerializer<string>(schemaRegistry, avroSerializerConfig).AsSyncOverAsync())
                //.SetValueSerializer(new AvroSerializer<User>(schemaRegistry, avroSerializerConfig).AsSyncOverAsync())
                .SetStatisticsHandler(
                    async (producer, json) =>
                    {
                        // Console.WriteLine($"Stats = {json}");
                        await PublishMetrics(appName, "produce", json);
                    })
                .SetErrorHandler(
                    (producer, error) =>
                    {
                        Console.WriteLine($"ErrorHandler Code = {error.Code}, Reason = {error.Reason}, IsBrokerError = {error.IsBrokerError}, IsFatal = {error.IsFatal}, IsLocalError = {error.IsLocalError}");
                        Console.WriteLine($"ErrorHandler Waiting for recovery. Poll for 3 seconds...");
                        producer.Poll(TimeSpan.FromSeconds(3));
                    })
                .Build())
            {
                //int numProduced = 0;
                int numMessages = 1000;
                for (int i = 0; i < numMessages; ++i)
                {
                    var key = "alice";
                    var val = new User
                    {
                        favorite_color = "Black",
                        favorite_number = i,
                        hourly_rate = 10.00M,
                        name = key
                    };
                    if (i%2 == 0) {
                      Thread.Sleep(500);
                    }
                    // Console.WriteLine($"Producing record: {key} {val}");
                    // produceDurable(producer, topic, key, val);
                    produceAsync(producer, topic, key, "Black");
                    //numProduced++;
                }

                Thread.Sleep(10000);

                producer.Flush(TimeSpan.FromSeconds(10));
            }

        }

        private static void produceDurable(IProducer<string, string> producer, string topic, string key, string val)
        {
            try
            {
                DeliveryResult<string, string> deliveryReport = producer.ProduceAsync(topic, new Message<string, string> { Key = key, Value = val }).Result;
                if (deliveryReport.Status == PersistenceStatus.Persisted)
                {
                    Console.WriteLine($"Produced message {deliveryReport.Message.Value} to: {deliveryReport.TopicPartitionOffset} : {deliveryReport.Timestamp.UnixTimestampMs}");
                }
                else
                {
                    Console.WriteLine($"Unable to persist message {val}");
                }
            }
            catch (ProduceException<string, string> ex)
            {
                Console.WriteLine($"ProduceException error producing message: ErrorCode {ex.Error.Code} {ex.Error.Reason} Reason={ErrorCodeExtensions.GetReason(ex.Error.Code)} {ex}");
                if (ex.Error.Code == ErrorCode.Local_QueueFull)
                {
                    //  Get callback execution to drain the queue
                    producer.Poll(TimeSpan.FromSeconds(3));
                }
                else if (ex.Error.Code == ErrorCode.BrokerNotAvailable ||
                    ex.Error.Code == ErrorCode.Local_AllBrokersDown ||
                    ex.Error.Code == ErrorCode.LeaderNotAvailable ||
                    ex.Error.Code == ErrorCode.Local_Fail ||
                    ex.Error.Code == ErrorCode.Local_Fatal)
                {
                    Console.WriteLine($"ERROR: Unhandled error {ex.Error.Code}");
                }
            }
            catch (KafkaException ex)
            {
                Console.WriteLine($"ERROR: KafkaException error producing message: {ex.Error.Code} {ex.Error.Reason} {ex}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"ERROR: Exception error producing message: {ex}");
            }
        }

        private static void produceAsync(IProducer<string, string> producer, string topic, string key, string val)
        {
            try
            {
                producer.Produce(topic, new Message<string, string> { Key = key, Value = val },
                    (deliveryReport) =>
                    {
                        if (deliveryReport.Error.Code != ErrorCode.NoError)
                        {
                            Console.WriteLine($"ERROR: Failed to deliver message: {deliveryReport.Message.Value}, Code {deliveryReport.Error.Code}, Reason {deliveryReport.Error.Reason}");
                        }
                        else
                        {
                            Console.WriteLine($"Produced message {deliveryReport.Message.Value} to: {deliveryReport.TopicPartitionOffset} : {deliveryReport.Timestamp.UnixTimestampMs}");
                        }
                    });
            }
            catch (ProduceException<string, string> ex)
            {
                Console.WriteLine($"ERROR: ProduceException error producing message: ErrorCode {ex.Error.Code} {ex.Error.Reason} Reason={ErrorCodeExtensions.GetReason(ex.Error.Code)} {ex}");
                if (ex.Error.Code == ErrorCode.Local_QueueFull)
                {
                    //  Get callback execution to drain the queue
                    producer.Poll(TimeSpan.FromSeconds(3));
                }
                else if (ex.Error.Code == ErrorCode.BrokerNotAvailable ||
                    ex.Error.Code == ErrorCode.Local_AllBrokersDown
                    ||
                    ex.Error.Code == ErrorCode.LeaderNotAvailable ||
                    ex.Error.Code == ErrorCode.Local_Fail ||
                    ex.Error.Code == ErrorCode.Local_Fatal)
                {
                    Console.WriteLine($"ERROR: Unhandled error {ex.Error.Code}");
                }
            }
            catch (KafkaException ex)
            {
                Console.WriteLine($"ERROR: KafkaException error producing message: {ex.Error.Code} {ex.Error.Reason} {ex}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"ERROR: Exception error producing message: {ex}");
            }
        }

        static async Task Consume(string topic, Dictionary<string, string> config)
        {
            // var schemaRegistry = CreateSchemaRegistryClient(config).Result;
            var consumerConfig = CreateConsumerConfig(config).Result;

            string appName = "Confluent_DotNet_Test";

            CancellationTokenSource cts = new CancellationTokenSource();
            Console.CancelKeyPress += (_, e) =>
            {
                e.Cancel = true; // prevent the process from terminating.
                cts.Cancel();
            };

            using (var consumer = new ConsumerBuilder<string, string>(consumerConfig)
                    // .SetKeyDeserializer(new AvroDeserializer<string>(schemaRegistry).AsSyncOverAsync())
                    // .SetValueDeserializer(new AvroDeserializer<User>(schemaRegistry).AsSyncOverAsync())
                    .SetStatisticsHandler(
                        async (consumer, json) =>
                        {
                            await PublishMetrics(appName, "consume", json);
                        })
                    .SetErrorHandler(
                        (consumer, error) =>
                        {
                            Console.WriteLine($"ErrorHandler Code = {error.Code}, Reason = {error.Reason}, IsBrokerError = {error.IsBrokerError}, IsFatal = {error.IsFatal}, IsLocalError = {error.IsLocalError}");
                            // Console.WriteLine($"ErrorHandler Waiting for recovery. Poll for 3 seconds...");
                            // TODO What to do in case of Error consumer.Poll(TimeSpan.FromSeconds(3));
                        })

                    .Build())
            {
                consumer.Subscribe(topic);
                var totalCount = 0;
                try
                {
                    while (true)
                    {
                        try
                        {
                            var cr = consumer.Consume(cts.Token);
                            var user = cr.Message.Value;
                            // Console.WriteLine($"Consumed record with key {cr.Message.Key} and value {cr.Message.Value}, {cr.Message.Timestamp.UnixTimestampMs}, {cr.TopicPartitionOffset} and updated total count to {totalCount}");
                            Console.WriteLine($"Consumed record {cr.Message.Timestamp.UnixTimestampMs}, {cr.TopicPartitionOffset} and updated total count to {totalCount}");
                            totalCount++;
                        }
                        catch (ConsumeException e)
                        {
                            Console.WriteLine($"Consume error: {e.Error.Reason}");
                        }
                    }
                }
                catch (OperationCanceledException)
                {
                    // Ctrl-C was pressed.
                    Console.WriteLine($" User interrupt. Exiting ...");
                }
                finally
                {
                    consumer.Close();
                }
            }
        }

        static void PrintUsage()
        {
            Console.WriteLine("usage: .. produce|consume <topic> <configPath> [<certDir>]");
            System.Environment.Exit(1);
        }

        static async Task Main(string[] args)
        {
            RootCommand rootCommand = new RootCommand(description: "Converts an image file from one format to another.")
            {
                TreatUnmatchedTokensAsErrors = true
            };

            var sc_produce = new Command("produce", "Kafka Dotnet Client - Producer")
            {
                TreatUnmatchedTokensAsErrors = true
            };
            rootCommand.Add(sc_produce);

            var sc_consume = new Command("consume", "Kafka Dotnet Client - Consumer")
            {
                TreatUnmatchedTokensAsErrors = true
            };
            rootCommand.Add(sc_consume);

            var o_topic = new Option<string>(aliases: ["--topic", "-t"], description: "Topic name")
            {
                IsRequired = true
            };

            var o_configPath = new Option<string>(aliases: ["--command.config", "-c"], description: "Client command config")
            {
                IsRequired = true
            };

            var o_certDir = new Option<string>(aliases: ["--cert.location", "-l"], description: "Certificate directory location when SSL is enabled");

            var o_publishMetrics = new Option<bool>(aliases: ["--publish-metrics", "-p"], description: "Publish client metrics", getDefaultValue: () => false)
            {
                Arity = ArgumentArity.Zero
            };

            rootCommand.AddGlobalOption(o_topic);
            rootCommand.AddGlobalOption(o_configPath);
            rootCommand.AddGlobalOption(o_certDir);
            rootCommand.AddGlobalOption(o_publishMetrics);

            sc_produce.SetHandler<string, string, string, bool>(
                async (topic, configPath, certDir, publishMetrics) =>
                {
                    Console.WriteLine($"--topic = {topic}");
                    Console.WriteLine($"--configPath = {configPath}");
                    Console.WriteLine($"--certDir = {certDir}");
                    Console.WriteLine($"--publishMetrics = {publishMetrics}");
                    var config = LoadConfig(configPath, certDir).Result;
                    CreateTopicMaybe(topic, 1, 3, config).Wait();
                    await Produce(topic, config);
                },
                o_topic, o_configPath, o_certDir, o_publishMetrics);


            sc_consume.SetHandler<string, string, string, bool>(
                async (topic, configPath, certDir, publishMetrics) =>
                {
                    Console.WriteLine($"--topic = {topic}");
                    Console.WriteLine($"--configPath = {configPath}");
                    Console.WriteLine($"--certDir = {certDir}");
                    Console.WriteLine($"--publishMetrics = {publishMetrics}");
                    var config = LoadConfig(configPath, certDir).Result;
                    CreateTopicMaybe(topic, 1, 3, config).Wait();
                    await Consume(topic, config);
                },
                o_topic, o_configPath, o_certDir, o_publishMetrics);

            await rootCommand.InvokeAsync(args);
        }
    }
}
