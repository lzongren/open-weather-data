package com.zliu.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

public class AbstractKafkaProducer<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractKafkaProducer.class);

    private Properties properties;
    private ProducerConfig producerConfig;
    private Producer<K, V> producer;

    private Properties getProperties() throws IOException {
        if (properties == null) {
            properties = new Properties();
            properties.load(AbstractKafkaProducer.class.getResourceAsStream("/config.properties"));
        }
        return properties;
    }

    public Producer<K, V> getProducer() throws IOException {
        if (producer == null) {
            producerConfig = new ProducerConfig(getProperties());
            producer = new Producer<K, V>(producerConfig);
        }
        return producer;
    }

    public void close() {
        if (producer != null) {
            producer.close();

            LOGGER.info("Producer is closed.");
        } else {
            LOGGER.info("Producer is not initialized, nothing to close.");
        }
    }
}
