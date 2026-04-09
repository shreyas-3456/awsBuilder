package com.awsBuilder.builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConfigurationPropertiesScan
@SpringBootApplication
public class BuilderApplication {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BuilderApplication.class);

	public static void main(String[] args) {
		System.out.println("===============================================");
		System.out.println("DEBUG >>> MAIN METHOD STARTED - BOOTING SPRING");
		System.out.println("===============================================");
		SpringApplication.run(BuilderApplication.class, args);
	}

	@Bean
	public org.springframework.boot.CommandLineRunner startupRunner() {
		return args -> {
			System.out.println("***********************************************");
			System.out.println("DEBUG >>> SPRING BOOT STARTED SUCCESSFULLY");
			System.out.println("***********************************************");
			logger.info(">>>>>>>>>> AWS BUILDER APPLICATION IS NOW ONLINE <<<<<<<<<<");
		};
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins("http://localhost:3000", "http://localhost:8080")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true)
						.maxAge(3600);
			}
		};
	}

}
