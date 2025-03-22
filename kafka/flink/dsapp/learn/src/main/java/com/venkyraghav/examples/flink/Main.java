package com.venkyraghav.examples.flink;

import com.venkyraghav.examples.flink.app.A0SimplePrint;
import com.venkyraghav.examples.flink.app.A1KafkaConsume;
import com.venkyraghav.examples.flink.app.A2KafkaCopy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

@Command(name = "flinkdslearn", version = "0.0.1",
        subcommands = {
            A0SimplePrint.class, A1KafkaConsume.class, A2KafkaCopy.class
        },
        mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class Main implements Callable<Integer>{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        Runtime.getRuntime().addShutdownHook(
//                new Thread(() -> {
//                    log.info("Flink app received shutdown request. Stopping components...");
//                    // TODO How to stop this?
//                }));

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
