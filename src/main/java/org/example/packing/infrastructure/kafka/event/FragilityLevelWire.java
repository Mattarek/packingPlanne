package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FragilityLevelWire {
	STANDARD("STANDARD", FragilityLevel.STANDARD),
	FRAGILE("FRAGILE", FragilityLevel.FRAGILE),
	ULTRA_FRAGILE("ULTRA_FRAGILE", FragilityLevel.ULTRA_FRAGILE);

	private static final Map<String, FragilityLevelWire> BY_WIRE_VALUE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(FragilityLevelWire::wireValue, Function.identity()));

	private static final Map<FragilityLevel, FragilityLevelWire> BY_DOMAIN = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(FragilityLevelWire::domain, Function.identity()));

	private final String wireValue;
	private final FragilityLevel domain;

	FragilityLevelWire(final String wireValue, final FragilityLevel domain) {
		this.wireValue = wireValue;
		this.domain = domain;
	}

	public String wireValue() {
		return wireValue;
	}

	public FragilityLevel domain() {
		return domain;
	}

	public static FragilityLevelWire fromDomain(final FragilityLevel fragility) {
		return Optional.ofNullable(BY_DOMAIN.get(fragility))
				.orElseThrow(() -> new IllegalStateException("No wire mapping for fragility level: " + fragility));
	}

	public static FragilityLevelWire fromWire(final String wireValue) {
		return Optional.ofNullable(wireValue)
				.map(BY_WIRE_VALUE::get)
				.orElseThrow(() -> new NonRetryableKafkaProcessingException("Unknown fragility level: " + wireValue));
	}
}
