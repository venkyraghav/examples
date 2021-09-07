package io.confluent.examples.fincard;

//import com.attunity.queue.msg.Headers;
//import com.attunity.queue.msg.loan.main.Data;
//import com.attunity.queue.msg.loan.main.DataRecord;
//import com.attunity.queue.msg.loan.main.KeyRecord;
//import com.attunity.queue.msg.operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.ByteBuffer;

@SpringBootApplication
public class FinCardApplication implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
//		KeyRecord keyRecord = KeyRecord.newBuilder()
//				.setLenderdatabaseid(Integer.valueOf(1234))
//				.setLoanrecordid(Integer.valueOf(456))
//				.build();
//		Data data = Data.newBuilder()
//				.setLenderdatabaseid(Integer.valueOf(1234))
//				.setLoanrecordid(Integer.valueOf(456))
//				.setLoanid("333333")
//				.setCustomergroupid(Integer.valueOf(111))
//				.setActualclosingdate("test")
//				.setLockexpirationdate("test2")
//				.setPropertylenderdatabaseid(Integer.valueOf(1234566))
//				.setPropertyrecordid(Integer.valueOf(989))
//				.build();
//		Data data2 = Data.newBuilder()
//				.setLenderdatabaseid(Integer.valueOf(1234))
//				.setLoanrecordid(Integer.valueOf(456))
//				.setLoanid("333333")
//				.setCustomergroupid(Integer.valueOf(111))
//				.setActualclosingdate("test")
//				.setLockexpirationdate("test2")
//				.setPropertylenderdatabaseid(Integer.valueOf(1234566))
//				.setPropertyrecordid(Integer.valueOf(989))
//				.build();
//		Headers head = Headers.newBuilder()
//				.setChangeMask(ByteBuffer.allocate(1))
//				.setChangeSequence("123")
//				.setColumnMask(ByteBuffer.allocate(1))
//				.setOperation(operation.INSERT)
//				.setStreamPosition("123")
//				.setTimestamp("123")
//				.setTransactionEventCounter(1L)
//				.setTransactionId("123")
//				.setTransactionLastEvent(false)
//				.build();
//
//		DataRecord dataRecord = new DataRecord(data,data2,head);
//		myProducer.sendMessage(keyRecord,dataRecord);
//		myProducer.sendMessage(keyRecord,dataRecord);
//		myProducer.sendMessage(keyRecord,dataRecord);
//		myProducer.sendMessage(keyRecord,dataRecord);
//
//
//		// myStream.test1();
	}

	public static void main(String[] args) {
		SpringApplication.run(FinCardApplication.class, args);
	}
}
