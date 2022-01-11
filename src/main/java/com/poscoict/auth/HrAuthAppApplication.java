package com.poscoict.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class HrAuthAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrAuthAppApplication.class, args);
	}
}
