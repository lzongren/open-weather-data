package com.zliu.consumer;

import com.zliu.repository.WeatherDataRepository;
import com.zliu.schema.WeatherData;

import org.apache.avro.Schema;
import org.apache.commons.lang.Validate;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public final class WeatherDataFlinkConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataFlinkConsumer.class);

    private final StreamExecutionEnvironment streamExecutionEnvironment;

    public WeatherDataFlinkConsumer() {
        streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    }

    private Properties properties;
    private Schema schema;

    public void init() throws IOException {
        if (properties == null) {
            properties = new Properties();
            properties.load(WeatherDataFlinkConsumer.class.getResourceAsStream("/config.properties"));

            LOGGER.info("Properties initialized.");
        }

        if (schema == null) {
            schema = new Schema.Parser().parse(WeatherDataFlinkConsumer.class.getResourceAsStream("/WeatherData.avsc"));

            LOGGER.info("Avro schema initialized.");
        }
    }

    private void ensureEnvironmentInitialized() {
        Validate.notNull(properties, "Flink properties must not be null");
        Validate.notNull(schema, "Schema must be provided");
    }

    public void run(final String topic) throws Exception {
        Validate.notNull(topic, "Topic must not be null");

        ensureEnvironmentInitialized();

        final DataStream<WeatherData> dataStream = streamExecutionEnvironment.addSource(new FlinkKafkaConsumer08<WeatherData>(
                topic,
                new AvroDeserializationSchema<WeatherData>(WeatherData.class, schema),
                properties));

        dataStream.rebalance()
                .map(new MapFunction<WeatherData, String>() {
                    public String map(WeatherData weatherData) throws Exception {
                        final String value = String.format("%s %s=%f", weatherData.getName().toString().replace(" ", ""), "value", weatherData.getMain().getTemp().floatValue());
                        return value;
                    }
                })
                .addSink(new SinkFunction<String>() {
                    @Override
                    public void invoke(String value) throws Exception {
                        WeatherDataRepository.INSTANCE.write("weather_data", value);
                    }
                });


        streamExecutionEnvironment.execute();
    }

    public static void main(final String[] args) throws Exception {
        final WeatherDataFlinkConsumer weatherDataFlinkConsumer = new WeatherDataFlinkConsumer();

        weatherDataFlinkConsumer.init();
        weatherDataFlinkConsumer.run("WeatherData");
    }
}
