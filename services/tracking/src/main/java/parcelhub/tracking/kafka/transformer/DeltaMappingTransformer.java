package parcelhub.tracking.kafka.transformer;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import parcelhub.tracking.kafka.dto.TrackingDelta;
import parcelhub.tracking.kafka.mapper.DeltaMapper;

public class DeltaMappingTransformer implements ValueTransformerWithKey<String, SpecificRecord, TrackingDelta> {

    private final DeltaMapper mapper;
    private ProcessorContext context;

    public DeltaMappingTransformer(DeltaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public TrackingDelta transform(String readOnlyKey, SpecificRecord value) {
        if (value == null) return null;
        long recordTs = context.timestamp();
        return mapper.map(value, recordTs).orElse(null);
    }

    @Override
    public void close() { }
}
