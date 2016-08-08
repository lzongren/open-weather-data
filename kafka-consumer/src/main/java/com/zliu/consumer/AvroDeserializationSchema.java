package com.zliu.consumer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.lang.Validate;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroDeserializationSchema<T> implements DeserializationSchema<T> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroDeserializationSchema.class);

    private final Class<T> avroType;
    private transient Schema schema;

    private transient DatumReader<T> reader;
    private transient Decoder decoder;

    public AvroDeserializationSchema(final Class<T> avroType, final Schema schema) {
        Validate.notNull("avroType must not be null");
        Validate.notNull("schema must not be null");

        this.avroType = avroType;
//        this.schema = schema;
    }

    @Override
    public T deserialize(byte[] message) {
        LOGGER.info("Will deserialize message to " + avroType.getSimpleName());

        ensureInitialized();

        try {
            if (schema == null) {
                schema = new Schema.Parser().parse(this.getClass().getResourceAsStream("/WeatherData.avsc"));
            }

            decoder = DecoderFactory.get().jsonDecoder(schema, new String(message));
            return reader.read(null, decoder);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize message to " + avroType.getSimpleName());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeExtractor.getForClass(avroType);
    }

    private void ensureInitialized() {
        if (reader == null) {
            if (org.apache.avro.specific.SpecificRecordBase.class.isAssignableFrom(avroType)) {
                reader = new SpecificDatumReader<T>(avroType);
            } else {
                reader = new ReflectDatumReader<T>(avroType);
            }
        }
    }
}
