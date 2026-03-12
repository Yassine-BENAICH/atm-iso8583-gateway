package com.atm.iso8583.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ATM ISO 8583 Gateway API")
                        .version("1.0.0")
                        .description("REST API Gateway for ISO 8583 ATM message processing"));
    }
}
