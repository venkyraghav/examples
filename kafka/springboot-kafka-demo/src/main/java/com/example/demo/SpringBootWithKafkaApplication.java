package com.example.demo;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@SpringBootApplication
public class SpringBootWithKafkaApplication {
	private static final Logger logger = LoggerFactory.getLogger(SpringBootWithKafkaApplication.class);

	private final MyProducer producer;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(SpringBootWithKafkaApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.run(args);
	}
	
	@Bean
	public CommandLineRunner CommandLineRunnerBean() {
		return (args) -> {
			for (String arg : args) {
				switch (arg) {
					case "--producer" -> {
						int maxIter = 100;
						for (int i=0;i<maxIter;i++) {
							if (true) {
								this.producer.sendMessageTransactional("awalther3", "t-shirts");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("htanaka3", "t-shirts");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("htanaka3", "batteries");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("eabara3", "t-shirts");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("htanaka3", "t-shirts");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("jsmith3", "book");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("awalther3", "t-shirts");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("jsmith3", "batteries");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("jsmith3", "gift card");
								// Thread.sleep(Duration.ofSeconds(10));
								this.producer.sendMessageTransactional("eabara3", "t-shirts");
							} 
							
							// Configuration.EntityT.Builder e = Configuration.EntityT.newBuilder();
							// Configuration.ConfigurationDetailsT.Builder cd = Configuration.ConfigurationDetailsT.newBuilder();
							// cd.setEntity(e.setEntityUseId(161).setOuId("80210602").build());

							// Configuration.ConfigurationT.Builder c = Configuration.ConfigurationT.newBuilder();
							// Configuration.ConfigurationT configuration = c.setEventId("46b6fc7c-cbe4-4f02-b0a6-71fb905d7701")
							// 	.setTimestamp(1750267834123L)
							// 	.setConfigurationType("ENTITY")
							// 	.setOperationType("UPDATE")
							// 	.setOrganizationId("80210602")
							// 	.addConfigurationDetails(cd)
							// 	.build();
							// this.producer.sendMessage("entityId", configuration);

							// c.clear();
							// cd.clear();

							// Configuration.ExemptionT.Builder e1 = Configuration.ExemptionT.newBuilder();
							// Configuration.ExemptionT e1e = e1.setExemptionId("123")
							// 	.setCertificateNumber("abc123")
							// 	.setJurisdictionId("1234")
							// 	.setEntityUseCode("789")
							// 	.setBusinessPartyId("3214123")
							// 	.setApplyToSubTJ(true)
							// 	.setEffectiveDate("2025-01-21T12:34:56Z")
							// 	.setExpirationDate("2025-01-21T12:34:56Z")
							// 	.setOnSkuDependencyType("1")
							// 	.addSkus("abc")
							// 	.addSkus("def")
							// 	.addSkus("ghi")
							// 	.build();
							// cd.setExemption(e1e);

							// Configuration.ConfigurationDetailsT.Builder cd2 = Configuration.ConfigurationDetailsT.newBuilder();
							// Configuration.ExemptionT.Builder e2 = Configuration.ExemptionT.newBuilder();
							// Configuration.ExemptionT e1e2 = e2.setExemptionId("123")
							// 	.setCertificateNumber("abc123")
							// 	.setJurisdictionId("892")
							// 	.setEntityUseCode("789")
							// 	.setBusinessPartyId("3214123")
							// 	.setApplyToSubTJ(true)
							// 	.setEffectiveDate("2025-01-21T12:34:56Z")
							// 	.setExpirationDate("2025-01-21T12:34:56Z")
							// 	.setOnSkuDependencyType("1")
							// 	.addSkus("1234")
							// 	.addSkus("5678")
							// 	.build();
							// cd2.setExemption(e1e2);

							// Configuration.ConfigurationT configuration2 = c.setEventId("46b6fc7c-cbe4-4f02-b0a6-71fb905d7701")
							// 	.setTimestamp(1750267834123L)
							// 	.setConfigurationType("EXEMPTION")
							// 	.setOperationType("UPDATE")
							// 	.setOrganizationId("80210603")
							// 	.addConfigurationDetails(cd)
							// 	.addConfigurationDetails(cd2)
							// 	.build();
							// this.producer.sendMessage("exemption", configuration2);

							logger.info("Sleeping for 2 minutes ...");
							Thread.sleep(Duration.ofMinutes(2));
						}
				}
					case "--consumer" -> {
						MessageListenerContainer listenerContainer = kafkaListenerEndpointRegistry.getListenerContainer("myConsumer2");
						listenerContainer.start();
				}
					default -> {
						System.out.println("Not implemented " + arg);
					}
				}
			}
		};
	}

	@Autowired
	SpringBootWithKafkaApplication(MyProducer producer) {
			this.producer = producer;
	}

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
}
