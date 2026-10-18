package com.devtrace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI devTraceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevTrace — Software Engineering Intelligence Platform API")
                        .description("REST API for repository ingestion, development process mining, code churn, hotspots, collaboration graphs, issue NLP, and PR risk estimation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DevTrace Engineering")
                                .url("https://github.com/Mithunsurya-Kumarasamy/devtrace"))
                        .license(new License().name("Apache 2.0")));
    }
}
