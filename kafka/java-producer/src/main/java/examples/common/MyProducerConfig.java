package examples.common;

import java.util.concurrent.atomic.AtomicLong;

public class MyProducerConfig {
    private boolean doConsume = true;
    private String transactionalId;
    private String groupInstanceId;
    private String clientId;
    private final AtomicLong messageRemaining = new AtomicLong(Long.MAX_VALUE);
    private String commandConfig;
    private String topic;
    private long payloadSize;
    private long payloadCount;
    private boolean transactional;
    private String bootstrapServer;

    public boolean isDoConsume() {
        return doConsume;
    }

    public void setDoConsume(boolean doConsume) {
        this.doConsume = doConsume;
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public void setTransactionalId(String transactionalId) {
        this.transactionalId = transactionalId;
    }

    public String getGroupInstanceId() {
        return groupInstanceId;
    }

    public void setGroupInstanceId(String groupInstanceId) {
        this.groupInstanceId = groupInstanceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long decrementMessageRemaining() {
        return messageRemaining.decrementAndGet();
    }

    public void setMessageRemaining(long newValue) {messageRemaining.set(newValue);}

    public String getCommandConfig() {
        return commandConfig;
    }

    public void setCommandConfig(String commandConfig) {
        this.commandConfig = commandConfig;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(long payloadSize) {
        this.payloadSize = payloadSize;
    }

    public long getPayloadCount() {
        return payloadCount;
    }

    public void setPayloadCount(long payloadCount) {
        this.payloadCount = payloadCount;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public String getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }
}
