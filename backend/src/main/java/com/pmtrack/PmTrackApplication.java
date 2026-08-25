package com.pmtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PmTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmTrackApplication.class, args);
    }
}
