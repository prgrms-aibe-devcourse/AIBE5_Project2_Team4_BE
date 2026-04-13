package com.ieum.ansimdonghaeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnsimdonghaengApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnsimdonghaengApplication.class, args);
    }
}
