package org.example.packing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class PackingApplication {

	static void main(final String[] args) {

		SpringApplication.run(PackingApplication.class, args);
	}
}
