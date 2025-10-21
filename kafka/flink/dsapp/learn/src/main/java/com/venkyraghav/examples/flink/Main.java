package com.venkyraghav.examples.flink;

import com.venkyraghav.examples.flink.app.A0SimplePrint;
import com.venkyraghav.examples.flink.app.A1KafkaConsume;
import com.venkyraghav.examples.flink.app.A2KafkaCopy;
import com.venkyraghav.examples.flink.model.Transaction;
import com.venkyraghav.examples.flink.serializer.TransactionSerde;
import com.venkyraghav.examples.flink.util.picocli.ArgException;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

//@Command(name = "flinkdslearn", version = "0.0.1",
//        subcommands = {
//            A0SimplePrint.class, A1KafkaConsume.class, A2KafkaCopy.class
//        },
//        mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class Main implements Callable<Integer>{
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static Properties props = new Properties();

    public static String getBootstrapServer(String prefix) {
        if (prefix == null || Utils.isBlank(prefix)) {
            return props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        } else {
            return props.getProperty(prefix + "." + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        }
    }

    public static Properties getProperties() {
        Properties retVal = new Properties();
        props.forEach((k,v) -> retVal.put(k, v));
        return retVal;
    }

    protected static Properties loadCommandConfig(String commandConfig) throws ArgException {
        if (Utils.isBlank(commandConfig)) {
            throw new ArgException("commandConfig value is blank");
        } else {
            try {
                boolean canRead = Paths.get(commandConfig).toFile().canRead();
                if (!canRead) {
                    throw new ArgException("File " + commandConfig + " is not readable");
                }
                props.load(new FileInputStream(commandConfig));
            } catch (Exception e) {
                throw new ArgException(e.getMessage());
            }
        }

        return props;
    }

//    public static void mainworking(String[] args) {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        String topic = "transactions";
//        try {
//            loadCommandConfig("/opt/flink/downloads/command_config.properties");
//
//            // set up a Kafka source
//            KafkaSource<Transaction> transactionSource =
//                    KafkaSource.<Transaction>builder()
//                            .setStartingOffsets(OffsetsInitializer.earliest())
//                            .setProperties(getProperties())
//                            .setBootstrapServers(getBootstrapServer(null))
//                            .setTopics(topic)
//                            .setValueOnlyDeserializer(new TransactionSerde())
//                            .build();
//
//            //DataStream<Transaction> transactionStream =
//            //    env.fromSource(transactionSource, WatermarkStrategy.noWatermarks(), "Transactions");
//
//            KafkaSink<Transaction> sink = KafkaSink.<Transaction>builder()
//                    .setKafkaProducerConfig(getProperties())
//                    .setBootstrapServers(getBootstrapServer(null))
//                    .setRecordSerializer(KafkaRecordSerializationSchema.builder()
//                            .setTopic(topic + "_copy")
//                            .setValueSerializationSchema(new TransactionSerde())
//                            .build())
//                    .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
//                    .build();
//
//            DataStream<Transaction> stream = env.fromSource(transactionSource, WatermarkStrategy.noWatermarks(), "Kafka Source");
//            stream.print();
//            stream.sinkTo(sink);
//
//            env.execute("Kafka Copier");
//            // Thread.sleep(0L);
//        } catch (Exception e) {
//            log.error("Exception", e);
//        }
//
//    }
    public static void main(String[] args) {
        CommandLine mainCommand = new CommandLine(new Main());
        try {
            ParseResult pr = mainCommand.parseArgs(args);
            if (pr == null || !pr.hasSubcommand()) {
                System.err.println("Missing subcommand");
                mainCommand.usage(System.err);
                System.exit(1);
            }
            int rc = mainCommand.execute(args);

            System.exit(rc);
        } catch(ParameterException e) {
            System.err.println("Exception " + e);
            e.getCommandLine().usage(System.err);
        } catch (Exception e) {
            System.err.println("Exception " + e);
        }
        System.exit(1);
    }

    @Override
    public Integer call() throws Exception {
        if (log.isInfoEnabled()) {log.info("Subcommand needed: '00_simpleprint', '01_kafkaconsume', etc ... ");}
        return 0;
    }
}
