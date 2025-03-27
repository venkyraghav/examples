package com.venkyraghav.examples.flink.app;

import com.venkyraghav.examples.flink.model.Transaction;
import com.venkyraghav.examples.flink.serializer.TransactionSerde;
import com.venkyraghav.examples.flink.util.ClientCommand;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "02_kafkacopy", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class A2KafkaCopy extends ClientCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(A2KafkaCopy.class);
    private StreamExecutionEnvironment env;

    @Override
    public Integer process() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        String topic = "transactions";

        // set up a Kafka source
        KafkaSource<Transaction> transactionSource =
            KafkaSource.<Transaction>builder()
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setProperties(getProperties())
                .setBootstrapServers(getBootstrapServer(null))
                .setTopics(topic)
                .setValueOnlyDeserializer(new TransactionSerde())
                .build();

        //DataStream<Transaction> transactionStream =
        //    env.fromSource(transactionSource, WatermarkStrategy.noWatermarks(), "Transactions");

        KafkaSink<Transaction> sink = KafkaSink.<Transaction>builder()
                .setKafkaProducerConfig(getProperties())
                .setBootstrapServers(getBootstrapServer(null))
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                    .setTopic(topic + "_copy")
                    .setValueSerializationSchema(new TransactionSerde())
                    .build())
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();
        try {
            /*
            transactionStream
                .keyBy(t -> t.t_id)
                .process(
                    new KeyedProcessFunction<Long, Transaction, Transaction>() {
                        // use Flink's managed keyed state
                        ValueState<Transaction> seen;

                        @Override
                        public void open(Configuration parameters) {
                            seen = getRuntimeContext().getState(new ValueStateDescriptor<>("seen", Transaction.class));
                        }

                        @Override
                        public void processElement(Transaction transaction,
                                KeyedProcessFunction<Long, Transaction, Transaction>.Context context,
                                Collector<Transaction> out) throws Exception {
                            if (seen.value() == null) {
                                seen.update(transaction);
                                // use timers to clean up state
                                context.timerService().registerProcessingTimeTimer(
                                        context.timerService().currentProcessingTime() + Duration.ofHours(1).toMillis());
                                out.collect(transaction);
                            }
                        }

                        @Override
                        public void onTimer(long timestamp,
                            KeyedProcessFunction<Long, Transaction, Transaction>.OnTimerContext ctx,
                            Collector<Transaction> out) {
                            seen.clear();
                        }
                    })
                    .sinkTo(sink).name("Copy transactions to transactions_copy");
                    */
            DataStream<Transaction> stream = env.fromSource(transactionSource, WatermarkStrategy.noWatermarks(), "Kafka Source");
            stream.print();
            stream.sinkTo(sink);

            env.execute("Kafka Copier");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
