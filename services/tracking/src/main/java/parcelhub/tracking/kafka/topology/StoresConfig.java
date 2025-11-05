package parcelhub.tracking.kafka.topology;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static parcelhub.tracking.kafka.topology.TopologyNames.STATUS_INDEX_STORE;

@Configuration
public class StoresConfig {

    @Bean(name = "statusIndexStoreBuilder")
    public StoreBuilder<?> statusIndexStoreBuilder() {
        Serde<String> s = Serdes.String();
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STATUS_INDEX_STORE),
                s, s
        );
    }
}
