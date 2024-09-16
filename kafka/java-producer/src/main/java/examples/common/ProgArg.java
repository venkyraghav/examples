package examples.common;


public enum ProgArg {
    HELP, BOOTSTRAP_SERVER, TOPIC, TRANSACTIONAL, PAYLOAD_SIZE, PAYLOAD_COUNT, COMMAND_CONFIG;

    public String getLongOption() {
        switch (this) {
            case HELP:
                return "help";
            case BOOTSTRAP_SERVER:
                return "bootstrap.server";
            case TOPIC:
                return "topic";
            case TRANSACTIONAL:
                return "transactional";
            case PAYLOAD_SIZE:
                return "payload.size";
            case PAYLOAD_COUNT:
                return "payload.count";
            case COMMAND_CONFIG:
                return "command.config";
        }
        return null;
    }

    public String getShortOption() {
        switch (this) {
            case HELP:
                return "h";
            case BOOTSTRAP_SERVER:
                return "b";
            case TOPIC:
                return "t";
            case TRANSACTIONAL:
                return "x";
            case PAYLOAD_SIZE:
                return "S";
            case PAYLOAD_COUNT:
                return "C";
            case COMMAND_CONFIG:
                return "c";
        }
        return null;
    }

    public boolean getHasArgument() {
        switch (this) {
            case HELP:
            case TRANSACTIONAL:
                return false;
        }
        return true;
    }

    public boolean isRequired() {
        switch (this) {
            case BOOTSTRAP_SERVER:
            case TOPIC:
                return true;
        }
        return false;
    }

    public String getDescription() {
        switch (this) {
            case HELP:
                return "Prints this help :-)";
            case BOOTSTRAP_SERVER:
                return "Bootstrap server";
            case TOPIC:
                return "Topic name";
            case TRANSACTIONAL:
                return "Use transactional producer";
            case PAYLOAD_SIZE:
                return "Payload size";
            case PAYLOAD_COUNT:
                return "Payload count";
            case COMMAND_CONFIG:
                return "Command config file";
        }
        return null;
    }
}
