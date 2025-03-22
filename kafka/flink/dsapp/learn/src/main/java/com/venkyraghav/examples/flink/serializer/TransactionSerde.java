package com.venkyraghav.examples.flink.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.venkyraghav.examples.flink.util.MyUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import com.venkyraghav.examples.flink.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

public class TransactionSerde implements DeserializationSchema<Transaction>, SerializationSchema<Transaction> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionSerde.class);

    @Override
    public Transaction deserialize(byte[] bytes) throws IOException {
        Transaction transaction = MyUtils.getMapper().readValue(bytes, Transaction.class);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Transaction = " + transaction);
        }
        return transaction;
    }

    @Override
    public boolean isEndOfStream(Transaction transaction) {
        return false;
    }

    @Override
    public TypeInformation<Transaction> getProducedType() {
        return TypeInformation.of(Transaction.class);
    }

    @Override
    public byte[] serialize(Transaction transaction) {
        try {
            byte[] bytes = MyUtils.getMapper().writeValueAsBytes(transaction);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Transaction_Copy = " + Base64.getEncoder().encodeToString(bytes));
            }
            return bytes;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing transaction", e);
        }
    }
}