package examples;

import examples.common.ConsumerCommand;
import examples.common.ProducerCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "kclient", version = "0.0.1", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true, subcommands = { ProducerCommand.class, ConsumerCommand.class })
public class KafkaClient implements Callable<Integer>{
    private static final Logger log = LoggerFactory.getLogger(KafkaClient.class);

    public static void main(final String[] args) throws Exception {
        CommandLine mainCommand = new CommandLine(new KafkaClient());
        try {
            ParseResult pr = mainCommand.parseArgs(args);
            if (pr == null || !pr.hasSubcommand()) {
                System.err.println("Missing subcommand");
                mainCommand.usage(System.err);
                System.exit(1);
            }
            int rc = mainCommand.execute(args);
            System.exit(rc);
        } catch(MissingParameterException e) {
            System.err.println("Exception " + e);
            e.getCommandLine().usage(System.err);
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
        if (log.isInfoEnabled()) {log.info("Subcommand needed: 'producer', 'consumer' ");}
        return 0;
    }
}