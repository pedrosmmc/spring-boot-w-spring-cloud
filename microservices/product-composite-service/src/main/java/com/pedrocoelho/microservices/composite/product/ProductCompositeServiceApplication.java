package com.pedrocoelho.microservices.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/* INFO: To enable Spring Boot's autoconfiguration feature to detect Spring beans in the api and util projects, we also need to add a @ComponentScan annotation to the main application class, which includes the pacjages o  the api and util projects. */
@SpringBootApplication
@ComponentScan("com.pedrocoelho")
public class ProductCompositeServiceApplication {

    /* INFO: The api* variables that are used to configure the OpenAPI bean are initialized from the property file using Spring @Value annotations. */
    @Value("${api.common.version}")
    String apiVersion;
    @Value("${api.common.title}")
    String apiTitle;
    @Value("${api.common.description}")
    String apiDescription;
    @Value("${api.common.termsOfService}")
    String apiTermsOfService;
    @Value("${api.common.license}")
    String apiLicense;
    @Value("${api.common.licenseUrl}")
    String apiLicenseUrl;
    @Value("${api.common.externalDocDesc}")
    String apiExternalDocDesc;
    @Value("${api.common.externalDocUrl}")
    String apiExternalDocUrl;
    @Value("${api.common.contact.name}")
    String apiContactName;
    @Value("${api.common.contact.url}")
    String apiContactUrl;
    @Value("${api.common.contact.email}")
    String apiContactEmail;

    /**
     * Defines a Spring Bean that returns an OpenAPI bean
     * Will exposed at $HOST:$PORT/swagger-ui.html
     *
     * @return OpenAPI documentation
     */
    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
            .info(new Info()
                .title(apiTitle)
                .description(apiDescription)
                .version(apiVersion)
                .contact(new Contact()
                    .name(apiContactName)
                    .url(apiContactUrl)
                    .email(apiContactEmail))
                .termsOfService(apiTermsOfService)
                .license(new License()
                    .name(apiLicense)
                    .url(apiLicenseUrl)))
            .externalDocs(new ExternalDocumentation()
                .description(apiExternalDocDesc)
                .url(apiExternalDocUrl));
    }

    /* INFO: The integration component uses a helper class in Spring Framework, RestTemplate.java, to perform the actual HTTP requests to the core microservices. Before we can inject it into the integration component, we need to configure it. */
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }

}
