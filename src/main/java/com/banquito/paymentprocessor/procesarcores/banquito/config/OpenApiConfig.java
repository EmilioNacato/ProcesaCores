package com.banquito.paymentprocessor.procesarcores.banquito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Procesamiento de Cores")
                .version("1.0")
                .description("API para procesar transacciones en los cores bancarios")
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")));
    }
} 