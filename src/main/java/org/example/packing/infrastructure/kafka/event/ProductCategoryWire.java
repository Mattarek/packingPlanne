package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ProductCategoryWire {
	STANDARD("STANDARD", ProductCategory.STANDARD),
	ELECTRONICS("ELECTRONICS", ProductCategory.ELECTRONICS),
	CLOTHING("CLOTHING", ProductCategory.CLOTHING),
	FOOD("FOOD", ProductCategory.FOOD),
	COSMETICS("COSMETICS", ProductCategory.COSMETICS),
	HAZARDOUS_MATERIAL("HAZARDOUS_MATERIAL", ProductCategory.HAZARDOUS_MATERIAL),
	FIREARMS_AND_AMMUNITION("FIREARMS_AND_AMMUNITION", ProductCategory.FIREARMS_AND_AMMUNITION),
	LIVE_ANIMALS("LIVE_ANIMALS", ProductCategory.LIVE_ANIMALS),
	ILLEGAL_SUBSTANCES("ILLEGAL_SUBSTANCES", ProductCategory.ILLEGAL_SUBSTANCES),
	HUMAN_REMAINS("HUMAN_REMAINS", ProductCategory.HUMAN_REMAINS);

	private static final Map<String, ProductCategoryWire> BY_WIRE_VALUE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(ProductCategoryWire::wireValue, Function.identity()));

	private static final Map<ProductCategory, ProductCategoryWire> BY_DOMAIN = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(ProductCategoryWire::domain, Function.identity()));

	private final String wireValue;
	private final ProductCategory domain;

	ProductCategoryWire(final String wireValue, final ProductCategory domain) {
		this.wireValue = wireValue;
		this.domain = domain;
	}

	public String wireValue() {
		return wireValue;
	}

	public ProductCategory domain() {
		return domain;
	}

	public static ProductCategoryWire fromDomain(final ProductCategory category) {
		return Optional.ofNullable(BY_DOMAIN.get(category))
				.orElseThrow(() -> new IllegalStateException("No wire mapping for product category: " + category));
	}

	public static ProductCategoryWire fromWire(final String wireValue) {
		return Optional.ofNullable(wireValue)
				.map(BY_WIRE_VALUE::get)
				.orElseThrow(() -> new NonRetryableKafkaProcessingException("Unknown product category: " + wireValue));
	}
}
