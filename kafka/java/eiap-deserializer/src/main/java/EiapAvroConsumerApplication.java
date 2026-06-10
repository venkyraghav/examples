import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public final class EiapAvroConsumerApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EiapAvroConsumerApplication.class);

    private EiapAvroConsumerApplication() {
    }

    public static void main(String[] args) throws IOException {
        EiapConsumerConfig config = EiapConsumerConfig.load();
        AtomicBoolean running = new AtomicBoolean(true);

        try (KafkaConsumer<String, SpecificRecord> consumer = new KafkaConsumer<>(config.toConsumerProperties())) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                consumer.wakeup();
            }));

            consumer.subscribe(Collections.singletonList(config.topic()));
            System.out.println("Consuming topic '" + config.topic() + "' with record-name strategy and OAuth/TLS enabled.");

            while (running.get()) {
                ConsumerRecords<String, SpecificRecord> records = consumer.poll(config.pollTimeout());
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SpecificRecord> record : records) {
                    logRecord(record);
                }

                consumer.commitSync();
            }
        } catch (org.apache.kafka.common.errors.WakeupException ignored) {
            if (running.get()) {
                throw ignored;
            }
        }
    }

    private static void logRecord(ConsumerRecord<String, SpecificRecord> record) {
        String eventTime = record.timestamp() > 0 ? Instant.ofEpochMilli(record.timestamp()).toString() : "n/a";
        SpecificRecord avroPojo = record.value();

        String schemaName = avroPojo == null ? "null" : avroPojo.getSchema().getFullName();
        String className = avroPojo == null ? "null" : avroPojo.getClass().getName();
        String pojoValue = avroPojo == null ? "null" : avroPojo.toString();

        System.out.printf(
            "partition=%d offset=%d key=%s timestamp=%s schema=%s class=%s pojo=%s%n",
            record.partition(),
            record.offset(),
            record.key(),
            eventTime,
            schemaName,
            className,
            pojoValue
        );
        switch (avroPojo) {
            case null -> {
                System.err.println("Null received");
            }
            case INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ADMISSION_BLOCKING_UPDATED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADMISSION_BLOCKING_UPDATED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ADV_CELL_SUP_DETECTION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADV_CELL_SUP_DETECTION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_ATTEMPT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_ATTEMPT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_RESULT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_RESULT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_AI_ACS_CONCEPT_DRIFT_DETECTION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_AI_ACS_CONCEPT_DRIFT_DETECTION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_CONFIG_MISSING event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_CONFIG_MISSING { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_HO_LEVEL_CHANGED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_HO_LEVEL_CHANGED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_NR_EVAL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_NR_EVAL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_NR_FREQ_REL_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_NR_FREQ_REL_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_PCI_REPORT_WANTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_PCI_REPORT_WANTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ANR_STOP_MEASURING event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ANR_STOP_MEASURING { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ASM_TRIGGERED_ALARM_SUPPRESSED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ASM_TRIGGERED_ALARM_SUPPRESSED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CANDNREL_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CANDNREL_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CANDNREL_REMOVE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CANDNREL_REMOVE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CA_SCELL_UPSWITCH event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CA_SCELL_UPSWITCH { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CA_WAITING_FOR_COVERAGE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CA_WAITING_FOR_COVERAGE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_DL_CAPACITY event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_DL_CAPACITY { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_SLEEP_CRITERIA_FAILED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_SLEEP_CRITERIA_FAILED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_SLEEP_PROHIBIT_TRIGGERED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_SLEEP_PROHIBIT_TRIGGERED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_WAKEUP_DETECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_WAKEUP_DETECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_WAKEUP_IND_RECEIVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_WAKEUP_IND_RECEIVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CELL_WAKEUP_TRIGGERED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CELL_WAKEUP_TRIGGERED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CMAS_REPET_STOPPED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CMAS_REPET_STOPPED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CMAS_REQ event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CMAS_REQ { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_CMAS_RESP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_CMAS_RESP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_COV_CELL_DISCOVERY_END event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_COV_CELL_DISCOVERY_END { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_COV_CELL_DISCOVERY_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_COV_CELL_DISCOVERY_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_COV_CELL_DISCOVERY_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_COV_CELL_DISCOVERY_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DLHARQ_ANMODE_CONFIG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DLHARQ_ANMODE_CONFIG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DL_COMP_MEAS_CONFIG_REJECT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DL_COMP_MEAS_CONFIG_REJECT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DL_COMP_MEAS_REP_DISCARD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DL_COMP_MEAS_REP_DISCARD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_STARTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_STARTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_STOPPED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_STOPPED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_UPDATED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_DYNAMIC_UE_ADMISSION_BLOCKING_UPDATED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ENDC_SETUP_BUF_MON_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ENDC_SETUP_BUF_MON_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ENDC_X2_CONN_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ENDC_X2_CONN_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ERAB_DATA_INFO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ERAB_DATA_INFO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ERAB_RELEASE_DELAYED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ERAB_RELEASE_DELAYED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ERAB_ROHC_FAIL_LIC_REJECT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ERAB_ROHC_FAIL_LIC_REJECT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ETWS_REPET_STOPPED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ETWS_REPET_STOPPED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ETWS_REQ event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ETWS_REQ { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ETWS_RESP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ETWS_RESP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_EUTRAN_FREQUENCY_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_EUTRAN_FREQUENCY_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_FILE_CONTENT_REMOVED_FOR_HIGHER_PRIORITY_FILES event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_FILE_CONTENT_REMOVED_FOR_HIGHER_PRIORITY_FILES { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_FLEX_FILTER_ISSUE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_FLEX_FILTER_ISSUE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_FREQ_REL_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_FREQ_REL_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_HO_WRONG_CELL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_HO_WRONG_CELL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_HO_WRONG_CELL_REEST event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_HO_WRONG_CELL_REEST { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_IMLB_ACTION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_IMLB_ACTION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_IMLB_CONTROL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_IMLB_CONTROL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_INTEGRITY_VER_FAIL_RRC_MSG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_INTEGRITY_VER_FAIL_RRC_MSG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_IP_ADDR_GET_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_IP_ADDR_GET_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LB_EVALUATION_TO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LB_EVALUATION_TO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LB_INTER_FREQ event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LB_INTER_FREQ { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LB_SUB_RATIO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LB_SUB_RATIO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LICENSE_UNAVAILABLE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LICENSE_UNAVAILABLE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LOAD_CONTROL_STATE_TRANSITION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LOAD_CONTROL_STATE_TRANSITION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_LOOPING_ENDC_FREQ_BLOCKLISTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_LOOPING_ENDC_FREQ_BLOCKLISTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MAX_FILESIZE_REACHED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MAX_FILESIZE_REACHED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MAX_FILESIZE_RECOVERY event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MAX_FILESIZE_RECOVERY { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MAX_UETRACES_REACHED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MAX_UETRACES_REACHED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MBMS_CELL_SELECTION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MBMS_CELL_SELECTION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MBMS_INTEREST_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MBMS_INTEREST_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEASUREMENT_REPORT_RECEIVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEASUREMENT_REPORT_RECEIVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A1 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A1 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A4 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A4 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A5 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A5 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_A6 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_A6 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B1_CDMA2000 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B1_CDMA2000 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B1_ENDC_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B1_ENDC_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B1_GERAN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B1_GERAN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B1_NR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B1_NR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B1_UTRA event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B1_UTRA { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B2_CDMA2000 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B2_CDMA2000 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B2_GERAN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B2_GERAN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B2_NR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B2_NR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_B2_UTRA event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_B2_UTRA { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_EUTRA event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_EUTRA { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_GERAN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_GERAN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_NR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_NR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_UTRA event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEAS_CONFIG_PERIODICAL_UTRA { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MEMORY_MECHANISM_STATE_TRANSITION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MEMORY_MECHANISM_STATE_TRANSITION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_MIMO_SLEEP_DETECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_MIMO_SLEEP_DETECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ML_GENERAL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ML_GENERAL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBCELL_ADDITIONAL_CGI event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBCELL_ADDITIONAL_CGI { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBCELL_CHANGE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBCELL_CHANGE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBENB_CHANGE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBENB_CHANGE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBGNB_CHANGE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBGNB_CHANGE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBREL_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBREL_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBREL_REMOVE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBREL_REMOVE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NEIGHBREL_SELECT_PCI_CONFLICT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NEIGHBREL_SELECT_PCI_CONFLICT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NO_RESET_ACK_FROM_MME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NO_RESET_ACK_FROM_MME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NO_SCELL_ON_NODE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NO_SCELL_ON_NODE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NR_FREQUENCY_EXCLUDED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NR_FREQUENCY_EXCLUDED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NR_PCI_CONFLICT_DETECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NR_PCI_CONFLICT_DETECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NR_PCI_CONFLICT_RESOLVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NR_PCI_CONFLICT_RESOLVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_NR_PCI_CONFLICT_SUSPECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_NR_PCI_CONFLICT_SUSPECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_OBSERVABILITY_STATE_CHANGE_IND event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_OBSERVABILITY_STATE_CHANGE_IND { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_ONGOING_UE_MEAS event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_ONGOING_UE_MEAS { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PARTITION_CONFIG_MISSING event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PARTITION_CONFIG_MISSING { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PCI_CONFLICT_DETECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PCI_CONFLICT_DETECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PCI_CONFLICT_RESOLVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PCI_CONFLICT_RESOLVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PIM_MEASUREMENTS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PIM_MEASUREMENTS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PM_DATA_COLLECTION_LOST event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PM_DATA_COLLECTION_LOST { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PM_EVENT_SUSPECTMARKED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PM_EVENT_SUSPECTMARKED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PM_RECORDING_FAULT_JVM event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PM_RECORDING_FAULT_JVM { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_RESERVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_RESERVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_RESERVED_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_RESERVED_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_RESERVED_STOP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_RESERVED_STOP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_RESERVED_SUSP_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_RESERVED_SUSP_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_RESERVED_SUSP_STOP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_RESERVED_SUSP_STOP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_CELL_TRAFFIC_LOAD_STATE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_CELL_TRAFFIC_LOAD_STATE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_OFFLOAD_MEASURED_UE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_OFFLOAD_MEASURED_UE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PTM_UE_HO_BLOCKED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PTM_UE_HO_BLOCKED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_PWS_RESTART_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_PWS_RESTART_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RECOMMENDED_NR_SI_UPDATES_REACHED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RECOMMENDED_NR_SI_UPDATES_REACHED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RESUME_LOW_ARP_DRB_DL_RLC_FAIL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RESUME_LOW_ARP_DRB_DL_RLC_FAIL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RETAIN_UECTXT_HIGH_ARP_DRB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RETAIN_UECTXT_HIGH_ARP_DRB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RIM_RAN_INFORMATION_RECEIVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RIM_RAN_INFORMATION_RECEIVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RIM_RAN_INFORMATION_SENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RIM_RAN_INFORMATION_SENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RIM_RAN_STATUS_CHANGED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RIM_RAN_STATUS_CHANGED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_CONNECTION_RELEASE_FAIL_NB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_CONNECTION_RELEASE_FAIL_NB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_ERROR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_ERROR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_ERROR_NB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_ERROR_NB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_RECONF_CATM_DATA_INACTIVITY_TIMER event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_RECONF_CATM_DATA_INACTIVITY_TIMER { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_SCELL_CONFIG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_SCELL_CONFIG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_SCELL_DECONFIGURED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_SCELL_DECONFIGURED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_RRC_UE_INFORMATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_RRC_UE_INFORMATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_S1AP_PROTOCOL_ERROR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_S1AP_PROTOCOL_ERROR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_S1_ERROR_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_S1_ERROR_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_S1_NAS_NON_DELIVERY_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_S1_NAS_NON_DELIVERY_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SCELL_ASM_CANDIDATE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SCELL_ASM_CANDIDATE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SCELL_CANDIDATE_ADD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SCELL_CANDIDATE_ADD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SCELL_CANDIDATE_REMOVE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SCELL_CANDIDATE_REMOVE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SON_OSCILLATION_DETECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SON_OSCILLATION_DETECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SON_UE_OSCILLATION_PREVENTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SON_UE_OSCILLATION_PREVENTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_SPID_PRIORITY_IGNORED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_SPID_PRIORITY_IGNORED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_TOO_EARLY_HO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_TOO_EARLY_HO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_TOO_LATE_HO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_TOO_LATE_HO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UETR_MEASUREMENT_REPORT_RECEIVED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UETR_MEASUREMENT_REPORT_RECEIVED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UETR_RRC_SCELL_CONFIG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UETR_RRC_SCELL_CONFIG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UETR_RRC_SCELL_DECONFIGURED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UETR_RRC_SCELL_DECONFIGURED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_ANR_CONFIG_PCI event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_ANR_CONFIG_PCI { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_ANR_CONFIG_PCI_REMOVE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_ANR_CONFIG_PCI_REMOVE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_ANR_PCI_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_ANR_PCI_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_CAPABILITY event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_CAPABILITY { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_ENDC_HO_MEAS event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_ENDC_HO_MEAS { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_ENDC_HO_QUAL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_ENDC_HO_QUAL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_LB_MEAS event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_LB_MEAS { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_LB_QUAL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_LB_QUAL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_MEAS_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_MEAS_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_MOBILITY_EVAL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_MOBILITY_EVAL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UE_MR_PHR_NB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UE_MR_PHR_NB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UNEXPECTED_RRC_MSG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UNEXPECTED_RRC_MSG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_UNEXPECTED_S1X2_MSG event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_UNEXPECTED_S1X2_MSG { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_WIFI_MOBILITY_EVAL_CONNECTED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_WIFI_MOBILITY_EVAL_CONNECTED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_WIFI_MOBILITY_EVAL_IDLE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_WIFI_MOBILITY_EVAL_IDLE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_X2AP_PROTOCOL_ERROR event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_X2AP_PROTOCOL_ERROR { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_X2_CONN_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_X2_CONN_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_EVENT_X2_ERROR_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_EVENT_X2_ERROR_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_BB_EENB_EVENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_BB_EENB_EVENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_BRANCH_UL_NOISEINTERF_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_BRANCH_UL_NOISEINTERF_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_BRANCH_UPPTS_UL_INTERFERENCE_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_BRANCH_UPPTS_UL_INTERFERENCE_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CAP_LICENSE_UTIL_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CAP_LICENSE_UTIL_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELLGROUP_MEAS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELLGROUP_MEAS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_AI_ACS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_AI_ACS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_DL_EMPTY_SUBFRAME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_DL_EMPTY_SUBFRAME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_DUCT_INTERFERENCE_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_DUCT_INTERFERENCE_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_MDT_M3_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_MDT_M3_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_NOISEINTERF_SC_NB_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_NOISEINTERF_SC_NB_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_PAGING_ATTEMPT_COUNT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_PAGING_ATTEMPT_COUNT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_QCI_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_QCI_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_TRAFFIC_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_TRAFFIC_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_TRAFFIC_REPORT2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_TRAFFIC_REPORT2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_CELL_TRAFFIC_REPORT3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_CELL_TRAFFIC_REPORT3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_EVENT_CMAS_REPET_COMPL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_EVENT_CMAS_REPET_COMPL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_EVENT_ETWS_REPET_COMPL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_EVENT_ETWS_REPET_COMPL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_PARTITION_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_PARTITION_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_PRB_LICENSE_UTIL_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_PRB_LICENSE_UTIL_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_PROCESSOR_LOAD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_PROCESSOR_LOAD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_PROCESSOR_LOAD_DYN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_PROCESSOR_LOAD_DYN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_CELL_CQI_SUBBAND event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_CELL_CQI_SUBBAND { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_CELL_MEASUREMENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_CELL_MEASUREMENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_CELL_MEASUREMENT_TDD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_CELL_MEASUREMENT_TDD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_CELL_NOISE_INTERFERENCE_PRB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_CELL_NOISE_INTERFERENCE_PRB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UE_MEASUREMENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UE_MEASUREMENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UE_MEASUREMENT_NB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UE_MEASUREMENT_NB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UE_MEASUREMENT_TA event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UE_MEASUREMENT_TA { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UTILIZATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UTILIZATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UTILIZATION2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UTILIZATION2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RADIO_UTILIZATION3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RADIO_UTILIZATION3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_RRC_SCELL_CONFIG_INFO event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_RRC_SCELL_CONFIG_INFO { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_SCELL_DET_ESTIMATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_SCELL_DET_ESTIMATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_SECTORCARRIER_EMF_POWER_CONTROL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_SECTORCARRIER_EMF_POWER_CONTROL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_SHARING_GROUP_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_SHARING_GROUP_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_THROUGHPUT_CELL_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_THROUGHPUT_CELL_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_BRANCH_UL_NOISEINTERF_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_BRANCH_UL_NOISEINTERF_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_BRANCH_UPPTS_UL_INTERFERENCE_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_BRANCH_UPPTS_UL_INTERFERENCE_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CAP_LICENSE_UTIL_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CAP_LICENSE_UTIL_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_DL_EMPTY_SUBFRAME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_DL_EMPTY_SUBFRAME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_PAGING_ATTEMPT_COUNT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_PAGING_ATTEMPT_COUNT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_QCI_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_QCI_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_CELL_TRAFFIC_REPORT3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_PARTITION_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_PARTITION_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_PRB_LICENSE_UTIL_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_PRB_LICENSE_UTIL_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_CELL_CQI_SUBBAND event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_CELL_CQI_SUBBAND { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_CELL_MEASUREMENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_CELL_MEASUREMENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_CELL_MEASUREMENT_TDD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_CELL_MEASUREMENT_TDD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_CELL_NOISE_INTERFERENCE_PRB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_CELL_NOISE_INTERFERENCE_PRB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_UE_MEASUREMENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_UE_MEASUREMENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_UTILIZATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_UTILIZATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_UTILIZATION2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_UTILIZATION2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_RADIO_UTILIZATION3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_RADIO_UTILIZATION3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_UE_ACTIVE_SESSION_TIME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_UE_ACTIVE_SESSION_TIME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_UE_LCG_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_UE_LCG_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_UE_RB_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_UE_RB_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UETR_UE_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UETR_UE_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_ACTIVE_SESSION_TIME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_ACTIVE_SESSION_TIME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_LCG_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_LCG_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M1_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M1_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M2_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M2_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M4_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M4_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M5_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M5_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M6_DL_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M6_DL_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M6_UL_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M6_UL_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_MDT_M7_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_MDT_M7_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_RB_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_RB_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PER_UE_TRAFFIC_REP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PER_UE_TRAFFIC_REP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ANR_CGI_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ANR_CGI_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ANR_PCI_CONFLICT_CGI_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ANR_PCI_CONFLICT_CGI_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_CELL_SLEEP_TRIGGERED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_CELL_SLEEP_TRIGGERED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_CSG_CELL_CGI_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_CSG_CELL_CGI_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_DNS_LOOKUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_DNS_LOOKUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ENDC_X2_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ENDC_X2_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ENDC_X2_TGNB_CONF_LOOKUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ENDC_X2_TGNB_CONF_LOOKUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ERAB_MODIFY event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ERAB_MODIFY { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ERAB_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ERAB_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ERAB_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ERAB_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_EXEC_S1_IN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_EXEC_S1_IN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_EXEC_S1_OUT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_EXEC_S1_OUT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_EXEC_X2_IN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_EXEC_X2_IN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_EXEC_X2_OUT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_EXEC_X2_OUT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_PREP_S1_IN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_PREP_S1_IN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_PREP_S1_OUT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_PREP_S1_OUT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_PREP_X2_IN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_PREP_X2_IN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_HO_PREP_X2_OUT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_HO_PREP_X2_OUT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_INITIAL_CTXT_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_INITIAL_CTXT_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_INTRA_CELL_HO_PREP_OUT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_INTRA_CELL_HO_PREP_OUT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_M3_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_M3_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_MBMS_SESSION_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_MBMS_SESSION_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_MBMS_SESSION_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_MBMS_SESSION_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_MIMO_SLEEP_SWITCHED event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_MIMO_SLEEP_SWITCHED { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ML_IMC_MEAS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ML_IMC_MEAS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ML_MEAS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ML_MEAS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ML_PA_MEAS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ML_PA_MEAS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_ML_PA_SCELLS_COV_MEAS_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_ML_PA_SCELLS_COV_MEAS_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_MN_MCG_RELOCATION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_MN_MCG_RELOCATION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_NAS_TRANSFER_DL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_NAS_TRANSFER_DL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_NEIGHBOR_PRB_UTIL_SUBSCRIPTION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_NEIGHBOR_PRB_UTIL_SUBSCRIPTION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_NON_PLANNED_PCI_CGI_REPORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_NON_PLANNED_PCI_CGI_REPORT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_REVERSE_DNS_LOOKUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_REVERSE_DNS_LOOKUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_RRC_CONNECTION_RE_ESTABLISHMENT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_RRC_CONNECTION_RE_ESTABLISHMENT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_RRC_CONN_RECONF_NO_MOB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_RRC_CONN_RECONF_NO_MOB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_RRC_CONN_RESUME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_RRC_CONN_RESUME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_RRC_CONN_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_RRC_CONN_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_S1_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_S1_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_S1_SIG_CONN_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_S1_SIG_CONN_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_S1_TENB_CONF_LOOKUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_S1_TENB_CONF_LOOKUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_S1_TGNB_CONF_LOOKUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_S1_TGNB_CONF_LOOKUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_SCTP_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_SCTP_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_SCTP_SHUTDOWN event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_SCTP_SHUTDOWN { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_SOFT_LOCK event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_SOFT_LOCK { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_TESTEVENT_COMPLEX event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_TESTEVENT_COMPLEX { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_TESTEVENT_MANY_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_TESTEVENT_MANY_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_TESTEVENT_MANY_STOP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_TESTEVENT_MANY_STOP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_TESTEVENT_NOPARAM_START event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_TESTEVENT_NOPARAM_START { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_TESTEVENT_POST event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_TESTEVENT_POST { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_UE_CTXT_FETCH event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_UE_CTXT_FETCH { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_UE_CTXT_MODIFY event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_UE_CTXT_MODIFY { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_UE_CTXT_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_UE_CTXT_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_UE_CTXT_RESUME event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_UE_CTXT_RESUME { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_UE_CTXT_SUSPEND event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_UE_CTXT_SUSPEND { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_X2_MN_INIT_SGNB_MOD event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_X2_MN_INIT_SGNB_MOD { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_X2_RESET event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_X2_RESET { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_X2_SETUP event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_X2_SETUP { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_PROC_X2_SGNB_ADDITION event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_PROC_X2_SGNB_ADDITION { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_BASIC event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_BASIC { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_BB_BBM event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_BB_BBM { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_BB_CELL event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_BB_CELL { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_BB_RB event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_BB_RB { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_BB_UE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_BB_UE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_CELL1 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_CELL1 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_CELL2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_CELL2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_EXT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_EXT { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_L3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_L3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_MANDATORY_OVERRIDE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_MANDATORY_OVERRIDE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_1 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_1 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_10 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_10 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_11 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_11 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_12 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_12 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_13 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_13 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_14 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_14 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_15 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_15 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_16 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_16 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_17 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_17 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_18 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_18 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_19 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_19 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_2 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_2 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_20 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_20 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_21 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_21 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_22 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_22 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_23 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_23 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_24 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_24 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_3 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_3 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_4 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_4 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_5 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_5 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_6 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_6 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_7 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_7 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_8 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_8 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_PROC_BASE_9 event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_PROC_BASE_9 { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_TESTEVENT_UE event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_TESTEVENT_UE { }", event.SCHEMA$.toString(true));
            }
            case INTERNAL_UE_MEAS_ABORT event -> {
              log.info("EIAP 4G AVRO Event : INTERNAL_UE_MEAS_ABORT { }", event.SCHEMA$.toString(true));
            }
            case M3_M3_SETUP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : M3_M3_SETUP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case M3_M3_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : M3_M3_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case M3_M3_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : M3_M3_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_START_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_START_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_START_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_START_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_START_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_START_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_STOP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_STOP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_STOP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_STOP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_UPDATE_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_UPDATE_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case M3_MBMS_SESSION_UPDATE_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : M3_MBMS_SESSION_UPDATE_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case M3_MCE_CONFIGURATION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : M3_MCE_CONFIGURATION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case M3_MCE_CONFIGURATION_UPDATE_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : M3_MCE_CONFIGURATION_UPDATE_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case M3_MCE_CONFIGURATION_UPDATE_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : M3_MCE_CONFIGURATION_UPDATE_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case M3_RESET event -> {
              log.info("EIAP 4G AVRO Event : M3_RESET { }", event.SCHEMA$.toString(true));
            }
            case RRC_CONNECTION_RESUME event -> {
              log.info("EIAP 4G AVRO Event : RRC_CONNECTION_RESUME { }", event.SCHEMA$.toString(true));
            }
            case RRC_CONNECTION_RESUME_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : RRC_CONNECTION_RESUME_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case RRC_CONNECTION_RESUME_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : RRC_CONNECTION_RESUME_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case RRC_CONNECTION_RE_ESTABLISHMENT event -> {
              log.info("EIAP 4G AVRO Event : RRC_CONNECTION_RE_ESTABLISHMENT { }", event.SCHEMA$.toString(true));
            }
            case RRC_CONNECTION_RE_ESTABLISHMENT_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : RRC_CONNECTION_RE_ESTABLISHMENT_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case RRC_CSFB_PARAMETERS_REQUEST_CDMA2000 event -> {
              log.info("EIAP 4G AVRO Event : RRC_CSFB_PARAMETERS_REQUEST_CDMA2000 { }", event.SCHEMA$.toString(true));
            }
            case RRC_CSFB_PARAMETERS_RESPONSE_CDMA2000 event -> {
              log.info("EIAP 4G AVRO Event : RRC_CSFB_PARAMETERS_RESPONSE_CDMA2000 { }", event.SCHEMA$.toString(true));
            }
            case RRC_DL_INFORMATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : RRC_DL_INFORMATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case RRC_DL_INFORMATION_TRANSFER_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_DL_INFORMATION_TRANSFER_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_HANDOVER_FROM_EUTRA_PREPARATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : RRC_HANDOVER_FROM_EUTRA_PREPARATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case RRC_INTER_FREQ_RSTD_MEASUREMENT_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_INTER_FREQ_RSTD_MEASUREMENT_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_IN_DEVICE_COEX_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_IN_DEVICE_COEX_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_LOGGED_MEASUREMENT_CONFIGURATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_LOGGED_MEASUREMENT_CONFIGURATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_MASTER_INFORMATION_BLOCK event -> {
              log.info("EIAP 4G AVRO Event : RRC_MASTER_INFORMATION_BLOCK { }", event.SCHEMA$.toString(true));
            }
            case RRC_MASTER_INFORMATION_BLOCK_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_MASTER_INFORMATION_BLOCK_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_MBMS_INTEREST_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_MBMS_INTEREST_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_MBSFNAREA_CONFIGURATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_MBSFNAREA_CONFIGURATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_MEASUREMENT_REPORT event -> {
              log.info("EIAP 4G AVRO Event : RRC_MEASUREMENT_REPORT { }", event.SCHEMA$.toString(true));
            }
            case RRC_MOBILITY_FROM_E_UTRA_COMMAND event -> {
              log.info("EIAP 4G AVRO Event : RRC_MOBILITY_FROM_E_UTRA_COMMAND { }", event.SCHEMA$.toString(true));
            }
            case RRC_MOBILITY_FROM_E_UTRA_COMMAND_EXT event -> {
              log.info("EIAP 4G AVRO Event : RRC_MOBILITY_FROM_E_UTRA_COMMAND_EXT { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RECONFIGURATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RECONFIGURATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RECONFIGURATION_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RECONFIGURATION_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RECONFIGURATION_COMPLETE_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RECONFIGURATION_COMPLETE_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RECONFIGURATION_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RECONFIGURATION_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_REJECT event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_REJECT { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_REJECT_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_REJECT_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RELEASE_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RELEASE_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_REQUEST_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_REQUEST_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_COMPLETE_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_COMPLETE_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REJECT event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REJECT { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REJECT_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REJECT_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REQUEST_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_RE_ESTABLISHMENT_REQUEST_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_SETUP event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_SETUP { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_SETUP_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_SETUP_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_SETUP_COMPLETE_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_SETUP_COMPLETE_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_RRC_CONNECTION_SETUP_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_RRC_CONNECTION_SETUP_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_SCG_FAILURE_INFORMATION_NR event -> {
              log.info("EIAP 4G AVRO Event : RRC_SCG_FAILURE_INFORMATION_NR { }", event.SCHEMA$.toString(true));
            }
            case RRC_SECURITY_MODE_COMMAND event -> {
              log.info("EIAP 4G AVRO Event : RRC_SECURITY_MODE_COMMAND { }", event.SCHEMA$.toString(true));
            }
            case RRC_SECURITY_MODE_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : RRC_SECURITY_MODE_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case RRC_SECURITY_MODE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : RRC_SECURITY_MODE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case RRC_SYSTEM_INFORMATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_SYSTEM_INFORMATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_SYSTEM_INFORMATION_BLOCK_TYPE_1 event -> {
              log.info("EIAP 4G AVRO Event : RRC_SYSTEM_INFORMATION_BLOCK_TYPE_1 { }", event.SCHEMA$.toString(true));
            }
            case RRC_SYSTEM_INFORMATION_BLOCK_TYPE_1_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_SYSTEM_INFORMATION_BLOCK_TYPE_1_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_SYSTEM_INFORMATION_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_SYSTEM_INFORMATION_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_ASSISTANCE_INFORMATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_ASSISTANCE_INFORMATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_CAPABILITY_ENQUIRY event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_CAPABILITY_ENQUIRY { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_CAPABILITY_ENQUIRY_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_CAPABILITY_ENQUIRY_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_CAPABILITY_INFORMATION event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_CAPABILITY_INFORMATION { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_CAPABILITY_INFORMATION_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_CAPABILITY_INFORMATION_NB { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_INFORMATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_INFORMATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case RRC_UE_INFORMATION_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : RRC_UE_INFORMATION_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case RRC_UL_DEDICATED_MESSAGE_SEGMENT event -> {
              log.info("EIAP 4G AVRO Event : RRC_UL_DEDICATED_MESSAGE_SEGMENT { }", event.SCHEMA$.toString(true));
            }
            case RRC_UL_HANDOVER_PREPARATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : RRC_UL_HANDOVER_PREPARATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case RRC_UL_INFORMATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : RRC_UL_INFORMATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case RRC_UL_INFORMATION_TRANSFER_MRDC event -> {
              log.info("EIAP 4G AVRO Event : RRC_UL_INFORMATION_TRANSFER_MRDC { }", event.SCHEMA$.toString(true));
            }
            case RRC_UL_INFORMATION_TRANSFER_NB event -> {
              log.info("EIAP 4G AVRO Event : RRC_UL_INFORMATION_TRANSFER_NB { }", event.SCHEMA$.toString(true));
            }
            case S1_CONNECTION_ESTABLISHMENT_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_CONNECTION_ESTABLISHMENT_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_DOWNLINK_NAS_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_DOWNLINK_NAS_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_DOWNLINK_NON_UE_ASSOCIATED_LPPA_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_DOWNLINK_NON_UE_ASSOCIATED_LPPA_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_DOWNLINK_S1_CDMA2000_TUNNELING event -> {
              log.info("EIAP 4G AVRO Event : S1_DOWNLINK_S1_CDMA2000_TUNNELING { }", event.SCHEMA$.toString(true));
            }
            case S1_DOWNLINK_S1_CDMA2000_TUNNELING_EXT event -> {
              log.info("EIAP 4G AVRO Event : S1_DOWNLINK_S1_CDMA2000_TUNNELING_EXT { }", event.SCHEMA$.toString(true));
            }
            case S1_DOWNLINK_UE_ASSOCIATED_LPPA_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_DOWNLINK_UE_ASSOCIATED_LPPA_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_CONFIGURATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_CONFIGURATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_CONFIGURATION_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_CONFIGURATION_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_CONFIGURATION_UPDATE_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_CONFIGURATION_UPDATE_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_CONFIGURATION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_CONFIGURATION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_CP_RELOCATION_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_CP_RELOCATION_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_DIRECT_INFORMATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_DIRECT_INFORMATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_ENB_STATUS_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_ENB_STATUS_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_MODIFICATION_CONFIRM event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_MODIFICATION_CONFIRM { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_MODIFICATION_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_MODIFICATION_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_MODIFY_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_MODIFY_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_MODIFY_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_MODIFY_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_RELEASE_COMMAND event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_RELEASE_COMMAND { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_RELEASE_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_RELEASE_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_RELEASE_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_RELEASE_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_ERAB_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_ERAB_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_ERROR_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_ERROR_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_CANCEL event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_CANCEL { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_CANCEL_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_CANCEL_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_COMMAND event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_COMMAND { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_NOTIFY event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_NOTIFY { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_PREPARATION_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_PREPARATION_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_HANDOVER_REQUIRED event -> {
              log.info("EIAP 4G AVRO Event : S1_HANDOVER_REQUIRED { }", event.SCHEMA$.toString(true));
            }
            case S1_INITIAL_CONTEXT_SETUP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_INITIAL_CONTEXT_SETUP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_INITIAL_CONTEXT_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_INITIAL_CONTEXT_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_INITIAL_CONTEXT_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_INITIAL_CONTEXT_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_INITIAL_UE_MESSAGE event -> {
              log.info("EIAP 4G AVRO Event : S1_INITIAL_UE_MESSAGE { }", event.SCHEMA$.toString(true));
            }
            case S1_KILL_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_KILL_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_KILL_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_KILL_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_LOCATION_REPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_LOCATION_REPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_LOCATION_REPORTING_CONTROL event -> {
              log.info("EIAP 4G AVRO Event : S1_LOCATION_REPORTING_CONTROL { }", event.SCHEMA$.toString(true));
            }
            case S1_LOCATION_REPORT_FAILURE_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_LOCATION_REPORT_FAILURE_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_CONFIGURATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_CONFIGURATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_CONFIGURATION_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_CONFIGURATION_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_CONFIGURATION_UPDATE_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_CONFIGURATION_UPDATE_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_CONFIGURATION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_CONFIGURATION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_CP_RELOCATION_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_CP_RELOCATION_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_DIRECT_INFORMATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_DIRECT_INFORMATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_MME_STATUS_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : S1_MME_STATUS_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case S1_NAS_NON_DELIVERY_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_NAS_NON_DELIVERY_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_OVERLOAD_START event -> {
              log.info("EIAP 4G AVRO Event : S1_OVERLOAD_START { }", event.SCHEMA$.toString(true));
            }
            case S1_OVERLOAD_STOP event -> {
              log.info("EIAP 4G AVRO Event : S1_OVERLOAD_STOP { }", event.SCHEMA$.toString(true));
            }
            case S1_PAGING event -> {
              log.info("EIAP 4G AVRO Event : S1_PAGING { }", event.SCHEMA$.toString(true));
            }
            case S1_PATH_SWITCH_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_PATH_SWITCH_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_PATH_SWITCH_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_PATH_SWITCH_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_PATH_SWITCH_REQUEST_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_PATH_SWITCH_REQUEST_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_PWS_RESTART_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_PWS_RESTART_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_REROUTE_NAS_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_REROUTE_NAS_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_RESET event -> {
              log.info("EIAP 4G AVRO Event : S1_RESET { }", event.SCHEMA$.toString(true));
            }
            case S1_RESET_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : S1_RESET_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case S1_S1_SETUP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_S1_SETUP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_S1_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_S1_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_S1_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_S1_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_SECONDARY_RAT_DATA_USAGE_REPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_SECONDARY_RAT_DATA_USAGE_REPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_THROUGHPUT_ESTIMATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_THROUGHPUT_ESTIMATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_THROUGHPUT_ESTIMATION_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_THROUGHPUT_ESTIMATION_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CAPABILITY_INFO_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CAPABILITY_INFO_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_MODIFICATION_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_MODIFICATION_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_MODIFICATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_MODIFICATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_MODIFICATION_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_MODIFICATION_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RELEASE_COMMAND event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RELEASE_COMMAND { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RELEASE_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RELEASE_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RELEASE_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RELEASE_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RESUME_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RESUME_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RESUME_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RESUME_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_RESUME_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_RESUME_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_SUSPEND_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_SUSPEND_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_UE_CONTEXT_SUSPEND_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_UE_CONTEXT_SUSPEND_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_UPLINK_NAS_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_UPLINK_NAS_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_UPLINK_NON_UE_ASSOCIATED_LPPA_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_UPLINK_NON_UE_ASSOCIATED_LPPA_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_UPLINK_S1_CDMA2000_TUNNELING event -> {
              log.info("EIAP 4G AVRO Event : S1_UPLINK_S1_CDMA2000_TUNNELING { }", event.SCHEMA$.toString(true));
            }
            case S1_UPLINK_UE_ASSOCIATED_LPPA_TRANSPORT event -> {
              log.info("EIAP 4G AVRO Event : S1_UPLINK_UE_ASSOCIATED_LPPA_TRANSPORT { }", event.SCHEMA$.toString(true));
            }
            case S1_WIFI_ACCESS_DECISION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_WIFI_ACCESS_DECISION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_WIFI_ACCESS_DECISION_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_WIFI_ACCESS_DECISION_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case S1_WRITE_REPLACE_WARNING_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : S1_WRITE_REPLACE_WARNING_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case S1_WRITE_REPLACE_WARNING_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : S1_WRITE_REPLACE_WARNING_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_EVENT_FEAT_NOT_AVAIL event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_EVENT_FEAT_NOT_AVAIL { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_EVENT_NOT_CONFIG event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_EVENT_NOT_CONFIG { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_GERAN1 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_GERAN1 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_GERAN2 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_GERAN2 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_INTERFREQ1 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_INTERFREQ1 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_INTERFREQ2 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_INTERFREQ2 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_INTRAFREQ1 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_INTRAFREQ1 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_INTRAFREQ2 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_INTRAFREQ2 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_EVENT_FEAT_NOT_AVAIL event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_EVENT_FEAT_NOT_AVAIL { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_EVENT_INTERRUPT event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_EVENT_INTERRUPT { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_EVENT_NOT_CONFIG event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_EVENT_NOT_CONFIG { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_GERAN event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_GERAN { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_INTERFREQ event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_INTERFREQ { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_INTRAFREQ event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_INTRAFREQ { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_SEC_UTRAN event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_SEC_UTRAN { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_UTRAN1 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_UTRAN1 { }", event.SCHEMA$.toString(true));
            }
            case UE_MEAS_UTRAN2 event -> {
              log.info("EIAP 4G AVRO Event : UE_MEAS_UTRAN2 { }", event.SCHEMA$.toString(true));
            }
            case X2_CELL_ACTIVATION_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_CELL_ACTIVATION_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_CELL_ACTIVATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_CELL_ACTIVATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_CELL_ACTIVATION_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_CELL_ACTIVATION_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_CONTEXT_FETCH_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_CONTEXT_FETCH_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_CONTEXT_FETCH_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_CONTEXT_FETCH_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_CONTEXT_FETCH_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_CONTEXT_FETCH_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_CONTEXT_FETCH_RESPONSE_ACCEPT event -> {
              log.info("EIAP 4G AVRO Event : X2_CONTEXT_FETCH_RESPONSE_ACCEPT { }", event.SCHEMA$.toString(true));
            }
            case X2_DATA_FORWARDING_ADDRESS_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : X2_DATA_FORWARDING_ADDRESS_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case X2_ENB_CONFIGURATION_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENB_CONFIGURATION_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENB_CONFIGURATION_UPDATE_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENB_CONFIGURATION_UPDATE_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENB_CONFIGURATION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENB_CONFIGURATION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_CONFIGURATION_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_CONFIGURATION_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_CONFIGURATION_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_CONFIGURATION_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_CONFIGURATION_UPDATE_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_CONFIGURATION_UPDATE_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_CONFIGURATION_UPDATE_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_CONFIGURATION_UPDATE_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_PARTIAL_RESET_CONFIRM event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_PARTIAL_RESET_CONFIRM { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_PARTIAL_RESET_REQUIRED event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_PARTIAL_RESET_REQUIRED { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_RESOURCE_STATUS_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_RESOURCE_STATUS_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_RESOURCE_STATUS_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_RESOURCE_STATUS_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_RESOURCE_STATUS_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_RESOURCE_STATUS_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_RESOURCE_STATUS_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_RESOURCE_STATUS_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_RRC_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_RRC_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_SGNB_MODIFICATION_CONFIRM event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_SGNB_MODIFICATION_CONFIRM { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_SGNB_MODIFICATION_REFUSE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_SGNB_MODIFICATION_REFUSE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_SGNB_MODIFICATION_REQUIRED event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_SGNB_MODIFICATION_REQUIRED { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_X2_SETUP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_X2_SETUP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_X2_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_X2_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_ENDC_X2_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_ENDC_X2_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_ERROR_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : X2_ERROR_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case X2_HANDOVER_CANCEL event -> {
              log.info("EIAP 4G AVRO Event : X2_HANDOVER_CANCEL { }", event.SCHEMA$.toString(true));
            }
            case X2_HANDOVER_PREPARATION_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_HANDOVER_PREPARATION_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_HANDOVER_REPORT event -> {
              log.info("EIAP 4G AVRO Event : X2_HANDOVER_REPORT { }", event.SCHEMA$.toString(true));
            }
            case X2_HANDOVER_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_HANDOVER_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_HANDOVER_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_HANDOVER_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_PRIVATE_MESSAGE event -> {
              log.info("EIAP 4G AVRO Event : X2_PRIVATE_MESSAGE { }", event.SCHEMA$.toString(true));
            }
            case X2_PROPRIETARY_CELL_SLEEP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_PROPRIETARY_CELL_SLEEP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_PROPRIETARY_CELL_SLEEP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_PROPRIETARY_CELL_SLEEP_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_PROPRIETARY_CELL_SLEEP_START_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_PROPRIETARY_CELL_SLEEP_START_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_PROPRIETARY_CELL_SLEEP_STOP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_PROPRIETARY_CELL_SLEEP_STOP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_PROPRIETARY_CELL_SLEEP_WAKEUP_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : X2_PROPRIETARY_CELL_SLEEP_WAKEUP_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case X2_RESET_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_RESET_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_RESET_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_RESET_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_RESOURCE_STATUS_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_RESOURCE_STATUS_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_RESOURCE_STATUS_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_RESOURCE_STATUS_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_RESOURCE_STATUS_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_RESOURCE_STATUS_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_RESOURCE_STATUS_UPDATE event -> {
              log.info("EIAP 4G AVRO Event : X2_RESOURCE_STATUS_UPDATE { }", event.SCHEMA$.toString(true));
            }
            case X2_RETRIEVE_UE_CONTEXT_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_RETRIEVE_UE_CONTEXT_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_RETRIEVE_UE_CONTEXT_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_RETRIEVE_UE_CONTEXT_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_RETRIEVE_UE_CONTEXT_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_RETRIEVE_UE_CONTEXT_RESPONSE { }", event.SCHEMA$.toString(true));
            }
            case X2_RLF_INDICATION event -> {
              log.info("EIAP 4G AVRO Event : X2_RLF_INDICATION { }", event.SCHEMA$.toString(true));
            }
            case X2_SECONDARY_RAT_DATA_USAGE_REPORT event -> {
              log.info("EIAP 4G AVRO Event : X2_SECONDARY_RAT_DATA_USAGE_REPORT { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_ACTIVITY_NOTIFICATION event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_ACTIVITY_NOTIFICATION { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_ADDITION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_ADDITION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_ADDITION_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_ADDITION_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_ADDITION_REQUEST_REJECT event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_ADDITION_REQUEST_REJECT { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_CHANGE_CONFIRM event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_CHANGE_CONFIRM { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_CHANGE_REFUSE event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_CHANGE_REFUSE { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_CHANGE_REQUIRED event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_CHANGE_REQUIRED { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_MODIFICATION_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_MODIFICATION_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_MODIFICATION_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_MODIFICATION_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_MODIFICATION_REQUEST_REJECT event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_MODIFICATION_REQUEST_REJECT { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RECONFIGURATION_COMPLETE event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RECONFIGURATION_COMPLETE { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RELEASE_CONFIRM event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RELEASE_CONFIRM { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RELEASE_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RELEASE_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RELEASE_REQUEST_ACKNOWLEDGE event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RELEASE_REQUEST_ACKNOWLEDGE { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RELEASE_REQUEST_REJECT event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RELEASE_REQUEST_REJECT { }", event.SCHEMA$.toString(true));
            }
            case X2_SGNB_RELEASE_REQUIRED event -> {
              log.info("EIAP 4G AVRO Event : X2_SGNB_RELEASE_REQUIRED { }", event.SCHEMA$.toString(true));
            }
            case X2_SN_STATUS_TRANSFER event -> {
              log.info("EIAP 4G AVRO Event : X2_SN_STATUS_TRANSFER { }", event.SCHEMA$.toString(true));
            }
            case X2_UE_CONTEXT_RELEASE event -> {
              log.info("EIAP 4G AVRO Event : X2_UE_CONTEXT_RELEASE { }", event.SCHEMA$.toString(true));
            }
            case X2_X2_SETUP_FAILURE event -> {
              log.info("EIAP 4G AVRO Event : X2_X2_SETUP_FAILURE { }", event.SCHEMA$.toString(true));
            }
            case X2_X2_SETUP_REQUEST event -> {
              log.info("EIAP 4G AVRO Event : X2_X2_SETUP_REQUEST { }", event.SCHEMA$.toString(true));
            }
            case X2_X2_SETUP_RESPONSE event -> {
              log.info("EIAP 4G AVRO Event : X2_X2_SETUP_RESPONSE { }", event.SCHEMA$.toString(true));
            }

            default -> throw new AssertionError();
        }
    }
}
