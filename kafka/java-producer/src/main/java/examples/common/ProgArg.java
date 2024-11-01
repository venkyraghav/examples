package examples.common;

public enum ProgArg {
    HELP, BOOTSTRAP_SERVER, TOPIC, TRANSACTIONAL, PAYLOAD_SIZE, PAYLOAD_COUNT, COMMAND_CONFIG, KEY_FORMAT, VALUE_FORMAT;

    public String getLongOption() {
        return switch (this) {
            case HELP -> "help";
            case BOOTSTRAP_SERVER -> "bootstrap.server";
            case TOPIC -> "topic";
            case TRANSACTIONAL -> "transactional";
            case PAYLOAD_SIZE -> "payload.size";
            case PAYLOAD_COUNT -> "payload.count";
            case COMMAND_CONFIG -> "command.config";
            case KEY_FORMAT -> "key.format";
            case VALUE_FORMAT -> "value.format";
            default -> null;
        };
    }

    public String getShortOption() {
        return switch (this) {
            case HELP -> "h";
            case BOOTSTRAP_SERVER -> "b";
            case TOPIC -> "t";
            case TRANSACTIONAL -> "x";
            case PAYLOAD_SIZE -> "S";
            case PAYLOAD_COUNT -> "C";
            case COMMAND_CONFIG -> "c";
            case KEY_FORMAT -> "k";
            case VALUE_FORMAT -> "v";
            default -> null;
        };
    }

    public boolean getHasArgument() {
        return switch (this) {
            case HELP, TRANSACTIONAL -> false;
            default -> true;
        };
    }

    public boolean isRequired() {
        return switch (this) {
            case BOOTSTRAP_SERVER, TOPIC -> true;
            default -> false;
        };
    }

    public String getDescription() {
        return switch (this) {
            case HELP -> "Prints this help :-)";
            case BOOTSTRAP_SERVER -> "Bootstrap server";
            case TOPIC -> "Topic name";
            case TRANSACTIONAL -> "Use transactional producer";
            case PAYLOAD_SIZE -> "Payload size";
            case PAYLOAD_COUNT -> "Payload count";
            case COMMAND_CONFIG -> "Command config file";
            case KEY_FORMAT -> "Key format(default:string). Supported formats:" + String.join(", ", Format.supportedFormats());
            case VALUE_FORMAT -> "Value format(default:string). Supported formats:" + String.join(", ", Format.supportedFormats());
            default -> null;
        };
    }
}