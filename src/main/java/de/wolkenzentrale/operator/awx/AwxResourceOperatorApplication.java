package de.wolkenzentrale.operator.awx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class AwxResourceOperatorApplication {

    public static void main(String[] args) {
        log.info("🚀 Starting AWX Resource Operator...");
        SpringApplication.run(AwxResourceOperatorApplication.class, args);
        log.info("✨ AWX Resource Operator started successfully! 🎉");
    }
} 