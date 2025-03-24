package com.venkyraghav.examples.flink.util;

import com.venkyraghav.examples.flink.util.picocli.ArgException;
import com.venkyraghav.examples.flink.util.picocli.FileExistsConverter;
import jdk.jshell.execution.Util;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;

public abstract class ClientCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-c", "--command.config"}, paramLabel = "COMMAND.CONFIG", description = "File with Configuration Parameters to Flink Client", required = false, converter = FileExistsConverter.class)
    private String commandConfig;

    protected volatile boolean keepRunning = true;

    protected abstract Integer process();
    protected abstract void cleanup();

    private Properties props;

    public String getBootstrapServer(String prefix) {
        if (prefix == null || Utils.isBlank(prefix)) {
            return props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        } else {
            return props.getProperty(prefix + "." + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        }
    }

    public Properties getProperties() {
        Properties retVal = new Properties();
        props.forEach((k,v) -> retVal.put(k, v));
        return retVal;
    }

    protected void validate() throws ArgException {
        props = loadCommandConfig(commandConfig);

        if (!props.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) || Utils.isBlank(props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))) {
            throw new ArgException(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG + " is required");
        }

        if (!props.containsKey(ConsumerConfig.GROUP_ID_CONFIG) || Utils.isBlank(ConsumerConfig.GROUP_ID_CONFIG)) {
            props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "noname-group");
        }

        if (!props.containsKey(ProducerConfig.CLIENT_ID_CONFIG) || Utils.isBlank(ProducerConfig.CLIENT_ID_CONFIG)) {
            props.setProperty(ProducerConfig.CLIENT_ID_CONFIG, props.getProperty(ConsumerConfig.GROUP_ID_CONFIG) + "-client");
        }
    }

    @Override
    public Integer call() throws Exception {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
            validate();
        } catch (MissingParameterException e) {
            System.out.println("Exception: " + e.getMessage());
            throw e;
        }
        return process();
    }

    protected Properties loadCommandConfig(String commandConfig) throws ArgException {
        Properties props = new Properties();
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
}