package com.finai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class FinAiApplication {

    private static final Logger log = LoggerFactory.getLogger(FinAiApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FinAiApplication.class);
        Environment env = app.run(args).getEnvironment();
        String port = env.getProperty("server.port", "8080");
        log.info("==================================================================");
        log.info("FinAI Application started successfully!");
        log.info("Access Dashboard : http://localhost:{}", port);
        log.info("Access Health API: http://localhost:{}/api/health", port);
        log.info("Access H2 Console: http://localhost:{}/h2-console", port);
        log.info("==================================================================");
    }
}
