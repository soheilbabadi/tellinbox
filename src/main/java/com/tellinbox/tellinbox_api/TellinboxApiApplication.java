package com.tellinbox.tellinbox_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TellinboxApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TellinboxApiApplication.class, args);
	}

}
