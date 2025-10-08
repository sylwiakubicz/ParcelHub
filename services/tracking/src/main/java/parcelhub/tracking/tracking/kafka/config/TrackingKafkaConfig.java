package parcelhub.tracking.tracking.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME;

@Configuration
@EnableKafkaStreams
public class TrackingKafkaConfig {

//    Can be also provided from application.properties if they are set there
//    @Autowired KafkaProperties kp;
//    Map<String, Object> props = kp.buildStreamsProperties();

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.application-id}")
    private String applicationId;

    @Value("${kafka.state-dir}")
    private String stateDir;

    @Value("${apicurio.url}")
    private String apicurioUrl;

    @Value("${iq.host}")
    private String iqHost;

    @Value("${server.port}")
    private String iqPort;

    @Bean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();

        props.put(APPLICATION_ID_CONFIG, applicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(STATE_DIR_CONFIG, stateDir);

        props.put(PROCESSING_GUARANTEE_CONFIG, EXACTLY_ONCE_V2);

        props.put(REPLICATION_FACTOR_CONFIG, 3);

        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), "lz4");

        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");

        props.put(STATESTORE_CACHE_MAX_BYTES_CONFIG, 104857600L); // ~100 MB
        props.put(COMMIT_INTERVAL_MS_CONFIG, 200);

        props.put(NUM_STANDBY_REPLICAS_CONFIG, 1);

        props.put("apicurio.registry.url", apicurioUrl);
        props.put("apicurio.registry.find-latest", true);

        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, iqHost + ":" + iqPort);

        return new KafkaStreamsConfiguration(props);
    }
}
