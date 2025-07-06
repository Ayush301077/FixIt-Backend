package com.fixit.FixIt.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FixItBackendApplication {

	public static void main(String[] args) {
		System.out.println("PORT ENV: " + System.getenv("PORT"));

		SpringApplication.run(FixItBackendApplication.class, args);
	}

}
