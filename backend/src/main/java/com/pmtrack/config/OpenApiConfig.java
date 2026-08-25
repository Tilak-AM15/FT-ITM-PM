package com.pmtrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("PMTrack - Integrated Project & Timesheet Management API")
                        .version("1.0.0")
                        .description("REST API Documentation for Project Management, Task Tracking, Employee Workspace, Timesheet Lifecycle, Approval Workflow, Reporting, RBAC, and Audit Logging.")
                        .contact(new Contact().name("PMTrack Team").email("support@futuretransformation.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
