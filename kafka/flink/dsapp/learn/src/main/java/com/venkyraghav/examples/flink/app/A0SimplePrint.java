package com.venkyraghav.examples.flink.app;

import java.time.LocalDate;

import com.venkyraghav.examples.flink.model.Customer;
import com.venkyraghav.examples.flink.util.ClientCommand;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

/**
 * Basic example of generating data and printing it.
 */
@Command(name = "00_simpleprint", mixinStandardHelpOptions = true, usageHelpAutoWidth = true, showDefaultValues = true)
public class A0SimplePrint extends ClientCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(A0SimplePrint.class);
    private StreamExecutionEnvironment env;

    @Override
    protected void cleanup() throws Exception {
        if (env != null) {
            if (LOGGER.isInfoEnabled()) {LOGGER.info("Cleaning up...");}
            env.close();
        }
    }

    public static final Customer[] CUSTOMERS = new Customer[] {
        new Customer(12L, "Alice", LocalDate.of(1984, 3, 12)),
        new Customer(32L, "Bob", LocalDate.of(1990, 10, 14)),
        new Customer(7L, "Kyle", LocalDate.of(1979, 2, 23)),
    };

    public Integer process() {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        try {
            for (int i = 0; i < 50; i++) {
                env.fromElements(CUSTOMERS)
                        .executeAndCollect()
                        .forEachRemaining(System.out::println);
                Thread.sleep(1000);
                System.out.println("Sleeping for 1 second in iteration " + i + " ...");
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Sleeping for 1 second in Iteration {}", i);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        } finally {
            try { cleanup(); } catch (Exception ignored){}
        }
        return 0;
    }
}
