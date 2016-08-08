package com.zliu.repository;

import org.apache.commons.lang3.Validate;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

public class WeatherDataRepository {

    private final InfluxDB influxDB;

    private WeatherDataRepository() {
        influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
    }

    public static WeatherDataRepository INSTANCE = new WeatherDataRepository();

    public void createDatabase(final String databaseName) {
        Validate.notNull(databaseName, "databaseName must not be null");

        influxDB.createDatabase(databaseName);
    }

    public void write(final String databaseName, final String records) {
        Validate.notNull(databaseName, "databaseName must not be null");
        Validate.notEmpty(records, "records must not be empty");

        influxDB.write(databaseName, "default", InfluxDB.ConsistencyLevel.ALL, records);
    }
}
