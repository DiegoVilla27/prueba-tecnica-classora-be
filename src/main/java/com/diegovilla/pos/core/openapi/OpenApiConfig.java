package com.diegovilla.pos.core.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the application's OpenAPI (Swagger) specification.
 *
 * <p>This configuration registers the {@link OpenAPI} bean consumed by Springdoc to automatically
 * generate the API documentation exposed by the application.
 *
 * <p>The generated specification includes:
 *
 * <ul>
 *   <li>General API metadata.
 *   <li>Application version and description.
 *   <li>Contact and license information.
 *   <li>Available server environments.
 *   <li>External project documentation.
 *   <li>JWT Bearer authentication scheme.
 *   <li>Global security requirements.
 * </ul>
 *
 * <p>Additional OpenAPI components such as reusable schemas, responses, examples, tags and request
 * bodies can be registered here as the application evolves.
 *
 * @author Diego Villa
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "Bearer Authentication";

    @Value("${application.version}")
    private String version;

    @Value("${application.name}")
    private String name;

    /**
     * Creates the application's OpenAPI definition.
     *
     * <p>Builds the root {@link OpenAPI} object containing all metadata, server definitions,
     * security schemes and documentation settings displayed by Swagger UI and other
     * OpenAPI-compatible tools.
     *
     * @return a fully configured {@link OpenAPI} instance.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(name)
                                .version(version)
                                .summary("REST API for the supermarket checkout (POS).")
                                .description(
                                        """
                                        Classora POS is a RESTful API built with Spring Boot following a
                                        hexagonal, feature-based architecture with DDD.

                                        Authenticated cashiers look up products by code and manage their
                                        sales: open, scan products, change quantities, remove lines,
                                        complete and cancel.
                                        """)
                                .contact(
                                        new Contact()
                                                .name("Diego Villa")
                                                .url("https://github.com/DiegoVilla27"))
                                .license(
                                        new License()
                                                .name("Apache License 2.0")
                                                .url(
                                                        "https://www.apache.org/licenses/LICENSE-2.0")))
                // Relative URL: works the same in local, docker, dev and prod
                .servers(List.of(new Server().url("/api/v1").description("Current environment")))
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Project Documentation")
                                .url("https://github.com/DiegoVilla27/prueba-tecnica-classora-be"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_SCHEME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        """
                                                        JSON Web Token authentication.

                                                        Obtain a token with POST /auth/login and enter only
                                                        the JWT value. The 'Bearer' prefix is added
                                                        automatically by Swagger UI.
                                                        """)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
