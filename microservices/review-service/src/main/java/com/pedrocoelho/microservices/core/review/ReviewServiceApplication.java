package com.pedrocoelho.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/* INFO: To enable Spring Boot's autoconfiguration feature to detect Spring beans in the api and util projects, we also need to add a @ComponentScan annotation to the main application class, which includes the pacjages o  the api and util projects. */
@SpringBootApplication
@ComponentScan("com.pedrocoelho")
public class ReviewServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
		String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
		LOG.info("Connected to MongoDB: {}", mysqlUri);
	}

}
