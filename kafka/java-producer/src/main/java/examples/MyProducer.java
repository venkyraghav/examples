package examples;

import examples.clients.ProducerBase;
import examples.clients.SimpleProducer;
import examples.clients.TransactionalProducer;
import examples.common.MyProducerArgs;
import examples.common.MyProducerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProducer {
    private static final Logger log = LoggerFactory.getLogger(MyProducer.class);
    private static final long finalSleep = 5_000;

    public static void main(final String[] args) throws Exception {
        MyProducer myProducer = new MyProducer();
        myProducer.doProcess(args);
    }

    public void doProcess(final String[] args) throws Exception {
        MyProducerArgs producerArgs = new MyProducerArgs();
        MyProducerConfig config = producerArgs.parseArgs(args);

        ProducerBase producer;
        if (config.isTransactional()) {
            producer = new TransactionalProducer(config);
        } else {
            producer = new SimpleProducer(config);
        }
        producer.run();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (log.isInfoEnabled()) {log.info("Shutting down gracefully after sleeping for {}ms ...", finalSleep);}
            config.setDoConsume(false);
            try {Thread.sleep(finalSleep);}catch (Exception ignored) {}
        }));

        try {if (log.isInfoEnabled()) {log.info("Sleeping {}ms", finalSleep);}Thread.sleep(finalSleep);}catch (Exception ignored) {}
    }
}