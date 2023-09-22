package com.autoprov.autoprov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class AutoprovisioningApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoprovisioningApplication.class, args);
	}

}
