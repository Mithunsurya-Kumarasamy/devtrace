package com.devtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DevTraceApplication {

    private static final Logger log = LoggerFactory.getLogger(DevTraceApplication.class);

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(DevTraceApplication.class, args);
    }

    private static void loadDotenv() {
        Path[] candidates = new Path[]{
                Paths.get(".env"),
                Paths.get("../.env"),
                Paths.get("../../.env")
        };
        for (Path p : candidates) {
            if (Files.exists(p)) {
                try (BufferedReader reader = Files.newBufferedReader(p)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).trim();
                            String val = line.substring(eq + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    log.info("Loaded environment variables from: {}", p.toAbsolutePath());
                    break;
                } catch (Exception e) {
                    log.warn("Could not load .env file from {}: {}", p, e.getMessage());
                }
            }
        }
    }
}
