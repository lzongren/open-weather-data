package com.zliu.producer;

import com.zliu.service.WeatherDataService;
import com.zliu.service.WeatherDataServiceImpl;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import kafka.producer.KeyedMessage;

public final class WeatherDataKafkaProducer extends AbstractKafkaProducer<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataKafkaProducer.class);

    public void publish(final String topic, final String key, final byte[] data) {
        Validate.notNull(topic, "topic must not be null");
        Validate.notNull(key, "key must not be null");
        Validate.notNull(data, "data must not be null");

        final KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(topic, key, new String(data));

        LOGGER.info("Will publish weather data to Kafka");
        try {
            getProducer().send(keyedMessage);
        } catch (final Exception e) {
            LOGGER.error("Failed to publish weather data to Kafka due to " + e);
        }
    }

    public static void main(String[] args) {
        final WeatherDataKafkaProducer weatherDataKafkaProducer = new WeatherDataKafkaProducer();
        final WeatherDataService weatherDataService = new WeatherDataServiceImpl();

        final String topic = "WeatherData";
        final String key = "weather";

        long counter = 1;
        long timeToSleep = TimeUnit.SECONDS.toMillis(5);
        boolean continueProducing = true;

        // TODO: more handling for failure
        while (continueProducing) {
            try {
                final byte[] data = weatherDataService.getData("Fribourg");
                weatherDataKafkaProducer.publish(topic, key, data);

                LOGGER.info("Published " + counter++ + "th data to Kafka topic " + topic);
                Thread.sleep(timeToSleep);

            } catch (final Exception e) {
                LOGGER.error("Failed to publish data to " + e);

                weatherDataKafkaProducer.close();
                continueProducing = false;
            }
        }
    }
}
