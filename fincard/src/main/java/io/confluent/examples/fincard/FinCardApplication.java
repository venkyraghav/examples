package io.confluent.examples.fincard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinCardApplication {
//	@Bean
//	public ObjectMapper objectMapper() {
//		ObjectMapper mapper = new ObjectMapper();
//		mapper.addMixIn(org.apache.avro.specific.SpecificRecord.class, JacksonIgnoreAvroPropertiesMixIn.class);
//		mapper.addMixIn(org.apache.avro.specific.SpecificData.class, JacksonIgnoreAvroPropertiesMixIn.class);
//		// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		// mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
//
//		return mapper;
//	}

	public static void main(String[] args) {
		SpringApplication.run(FinCardApplication.class, args);
	}

	//@Bean
	//public AvroModule avroDataFormat() {
//		return new AvroModule();
//	}
}
