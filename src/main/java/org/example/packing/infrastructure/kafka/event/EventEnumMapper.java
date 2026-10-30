package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EventEnumMapper {

	private static final Map<String, ProductCategory> CATEGORIES_BY_WIRE =
			indexByWire(ProductCategory.values(), EventEnumMapper::toWire);

	private static final Map<String, FragilityLevel> FRAGILITIES_BY_WIRE =
			indexByWire(FragilityLevel.values(), EventEnumMapper::toWire);

	private EventEnumMapper() {
	}

	public static String toWire(final ProductCategory category) {
		return switch (category) {
			case STANDARD -> "STANDARD";
			case ELECTRONICS -> "ELECTRONICS";
			case CLOTHING -> "CLOTHING";
			case FOOD -> "FOOD";
			case COSMETICS -> "COSMETICS";
			case HAZARDOUS_MATERIAL -> "HAZARDOUS_MATERIAL";
			case FIREARMS_AND_AMMUNITION -> "FIREARMS_AND_AMMUNITION";
			case LIVE_ANIMALS -> "LIVE_ANIMALS";
			case ILLEGAL_SUBSTANCES -> "ILLEGAL_SUBSTANCES";
			case HUMAN_REMAINS -> "HUMAN_REMAINS";
		};
	}

	public static ProductCategory categoryFromWire(final String wireValue) {
		return fromWire(CATEGORIES_BY_WIRE, wireValue, "product category");
	}

	public static String toWire(final FragilityLevel fragility) {
		return switch (fragility) {
			case STANDARD -> "STANDARD";
			case FRAGILE -> "FRAGILE";
			case ULTRA_FRAGILE -> "ULTRA_FRAGILE";
		};
	}

	public static FragilityLevel fragilityFromWire(final String wireValue) {
		return fromWire(FRAGILITIES_BY_WIRE, wireValue, "fragility level");
	}

	private static <E extends Enum<E>> Map<String, E> indexByWire(
			final E[] values,
			final Function<E, String> toWire
	) {
		return Arrays.stream(values).collect(Collectors.toUnmodifiableMap(toWire, Function.identity()));
	}

	private static <E> E fromWire(final Map<String, E> byWire, final String wireValue, final String label) {
		final E value = wireValue == null ? null : byWire.get(wireValue);
		if (value == null) {
			throw new NonRetryableKafkaProcessingException("Unknown " + label + ": " + wireValue);
		}
		return value;
	}
}
