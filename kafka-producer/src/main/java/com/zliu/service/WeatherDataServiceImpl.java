package com.zliu.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class WeatherDataServiceImpl implements WeatherDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataServiceImpl.class);

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String APP_KEY = "e1fc021d00c3fbdeadd99dc5c7b0fe58";

    private final static String REQUEST_METHOD = "GET";
    private final static String REQUEST_ACCEPT = "ACCEPT";
    private final static String REQUEST_TYPE = "application/json";


    private URL createUrl(final String city) throws MalformedURLException {
        return new URL(String.format("%sq=%s&APPID=%s", BASE_URL, city, APP_KEY));
    }

    public byte[] getData(final String city) {
        Validate.notEmpty(city, "city must not be empty");

        URL url;
        byte[] data = null;

        try {
            url = createUrl(city);
            LOGGER.info("Will request data from " + url.toURI().toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setRequestProperty(REQUEST_ACCEPT, REQUEST_TYPE);

            data = IOUtils.toByteArray(connection.getInputStream());
            connection.disconnect();

            LOGGER.info("Fetched weather data from " + url.getPath());
        } catch (final Exception e) {
            LOGGER.error("Failed to get data due to exception " + e);
        }

        return data;
    }

    public static void main(final String[] args) throws Exception {
        final WeatherDataService weatherDataService = new WeatherDataServiceImpl();

        System.out.println(new String(weatherDataService.getData("Fribourg")));
    }
}
