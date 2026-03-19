package com.atm.iso8583.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI iso8583GatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ATM ISO 8583 Gateway API")
                        .description("""
                                REST API Gateway for ISO 8583 ATM/payment message processing.
                               \s
                                **Flow:**
                                1. Client sends JSON request → API converts to ISO 8583 → sends to switch
                                2. Switch returns ISO 8583 response → API converts to JSON → returns to client
                               \s
                                Supports: Authorization (0100/0110), Reversal (0400/0410),\s
                                Network Management (0800/0810), Financial Request (0200/0210)
                               \s""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ATM Gateway Team")
                                .email("gateway@atm.com"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
