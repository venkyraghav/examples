package examples.common;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.kafka.common.utils.Utils;

import picocli.CommandLine.MissingParameterException;

public abstract class ClientCommand implements Callable<Integer> {
    protected abstract void validate() throws ArgException;
    protected abstract Integer process() throws Exception;

    @Override
    public Integer call() throws Exception {
        try {
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
        } else if (!Utils.isBlank(commandConfig)) {
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
