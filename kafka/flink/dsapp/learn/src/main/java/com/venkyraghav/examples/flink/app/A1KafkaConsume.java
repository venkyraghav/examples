package com.venkyraghav.examples.flink.app;

import com.venkyraghav.examples.flink.serializer.TransactionSerde;
import com.venkyraghav.examples.flink.model.Transaction;
import com.venkyraghav.examples.flink.util.ClientCommand;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import java.time.Duration;

@Command(name = "01_kafkaconsume", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class A1KafkaConsume extends ClientCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(A1KafkaConsume.class);

    @Override
    public Integer process() {
        // set up a Kafka source
        KafkaSource<Transaction> transactionSource =
            KafkaSource.<Transaction>builder()
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setProperties(getProperties())
                .setBootstrapServers(getBootstrapServer(null))
                .setTopics("transactions")
                .setValueOnlyDeserializer(new TransactionSerde())
                .build();

        DataStream<Transaction> transactionStream =
            env.fromSource(transactionSource, WatermarkStrategy.noWatermarks(), "Transactions");

        try {
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
                .executeAndCollect()
                .forEachRemaining(e -> {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(e.toString());
                    }
                });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
