package examples.common;

import org.apache.commons.cli.*;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class MyProducerArgs {
    private static final Logger log = LoggerFactory.getLogger(MyProducerArgs.class);

    private boolean hasArgument (CommandLine cmd, ProgArg p) {
        return cmd.hasOption(p.getShortOption()) || cmd.hasOption(p.getLongOption());
    }

    private String getArgument(CommandLine cmd, ProgArg p) {
        if (cmd.hasOption(p.getShortOption())) {
            return cmd.getOptionValue(p.getShortOption());
        } else if (cmd.hasOption(p.getLongOption())) {
            return cmd.getOptionValue(p.getLongOption());
        }
        return null;
    }

    private long getLongArgument(CommandLine cmd, ProgArg p) {
        String val = getArgument(cmd, p);
        if (val != null) {
            return Long.parseLong(val);
        }
        return 0L;
    }

    public MyProducerConfig parseArgs(final String[] args) {
        Options options = new Options();
        for (ProgArg p : ProgArg.values()) {
            if (p.isRequired()) {
                options.addRequiredOption(p.getShortOption(), p.getLongOption(), p.getHasArgument(), p.getDescription());
            } else {
                options.addOption(p.getShortOption(), p.getLongOption(), p.getHasArgument(), p.getDescription());
            }
        }

        CommandLineParser parser = new DefaultParser();
        MyProducerConfig config = new MyProducerConfig();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (hasArgument(cmd, ProgArg.HELP)) {
                if (log.isInfoEnabled()) {log.info("Printing help ...");}
                printHelp(options);
            }
            config.setTransactional (hasArgument(cmd, ProgArg.TRANSACTIONAL));
            config.setBootstrapServer (getArgument(cmd, ProgArg.BOOTSTRAP_SERVER));
            config.setTopic (getArgument(cmd, ProgArg.TOPIC));
            config.setPayloadCount (getLongArgument(cmd, ProgArg.PAYLOAD_COUNT));
            config.setPayloadSize (getLongArgument(cmd, ProgArg.PAYLOAD_SIZE));
            config.setCommandConfig (getArgument(cmd, ProgArg.COMMAND_CONFIG));
            config.setKeyFormat (Format.lookup(getArgument(cmd, ProgArg.KEY_FORMAT)));
            config.setValueFormat (Format.lookup(getArgument(cmd, ProgArg.VALUE_FORMAT)));

            validateArgs(config);

        } catch (ParseException|ArgException e) {
            log.error("Error: " + e.getMessage());
            printHelp(options);
        }
        return config;
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MyProducer", options, true);
        System.exit(1);
    }

    private void validateArgs(MyProducerConfig config) throws ArgException {
        if (Utils.isBlank(config.getBootstrapServer())) {
            throw new ArgException(ProgArg.BOOTSTRAP_SERVER.getDescription() + " is required");
        }
        if (config.getPayloadCount() <= 0) {
            throw new ArgException(ProgArg.PAYLOAD_COUNT.getDescription() + " should be > 0");
        }
        if (config.getPayloadSize() <= 0) {
            throw new ArgException(ProgArg.PAYLOAD_SIZE.getDescription() + " should be > 0");
        }
        if (Utils.isBlank(config.getTopic())) {
            throw new ArgException(ProgArg.TOPIC.getDescription() + " is required");
        }
        if (!Utils.isBlank(config.getCommandConfig())) {
            try {
                boolean canRead = Paths.get(config.getCommandConfig()).toFile().canRead();
                if (!canRead) {
                    throw new ArgException(ProgArg.COMMAND_CONFIG.getDescription() + " is not readable");
                }
            } catch (Exception e) {
                throw new ArgException(e.getMessage());
            }
        }
        if (config.getKeyFormat().isSupported() == false) {
            throw new ArgException(ProgArg.KEY_FORMAT.getDescription());
        }
        if (config.getValueFormat().isSupported() == false) {
            throw new ArgException(ProgArg.VALUE_FORMAT.getDescription());
        }
    }
    
}
