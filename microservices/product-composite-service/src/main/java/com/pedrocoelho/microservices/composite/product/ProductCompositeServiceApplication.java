package com.pedrocoelho.microservices.composite.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/* INFO: To enable Spring Boot's autoconfiguration feature to detect Spring beans in the api and util projects, we also need to add a @ComponentScan annotation to the main application class, which includes the pacjages o  the api and util projects. */
@SpringBootApplication
@ComponentScan("com.pedrocoelho")
public class ProductCompositeServiceApplication {

	/* INFO: The integration component uses a helper class in Spring Framework, RestTemplate.java, to perform the actual HTTP requests to the core microservices. Before we can inject it into the integration component, we need to configure it. */
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

}
