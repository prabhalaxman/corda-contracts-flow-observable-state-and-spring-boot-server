package com.sample.bankvendor.server.java;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ServerJava {

    /**
     * Spring Bean that binds a Corda Jackson object-mapper to HTTP message types used in Spring.
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    /**
     * Starts our Spring Boot application.
     */
    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(ServerJava.class);
        app.setBannerMode(Banner.Mode.OFF);
        //app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }
}