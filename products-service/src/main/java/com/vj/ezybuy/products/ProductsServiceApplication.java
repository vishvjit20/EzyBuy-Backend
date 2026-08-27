package com.vj.ezybuy.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class ProductsServiceApplication {

	public static void main(String[] args) {
		System.out.println("JVM Timezone: " + TimeZone.getDefault());
		SpringApplication.run(ProductsServiceApplication.class, args);
	}

}
