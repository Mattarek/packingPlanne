package org.example.packing.config;

import org.example.packing.application.port.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class SystemDependenciesConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public IdGenerator idGenerator() {
		return UUID::randomUUID;
	}
}
