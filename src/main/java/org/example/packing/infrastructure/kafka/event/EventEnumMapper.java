package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;

public final class EventEnumMapper {

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
		return switch (wireValue) {
			case "STANDARD" -> ProductCategory.STANDARD;
			case "ELECTRONICS" -> ProductCategory.ELECTRONICS;
			case "CLOTHING" -> ProductCategory.CLOTHING;
			case "FOOD" -> ProductCategory.FOOD;
			case "COSMETICS" -> ProductCategory.COSMETICS;
			case "HAZARDOUS_MATERIAL" -> ProductCategory.HAZARDOUS_MATERIAL;
			case "FIREARMS_AND_AMMUNITION" -> ProductCategory.FIREARMS_AND_AMMUNITION;
			case "LIVE_ANIMALS" -> ProductCategory.LIVE_ANIMALS;
			case "ILLEGAL_SUBSTANCES" -> ProductCategory.ILLEGAL_SUBSTANCES;
			case "HUMAN_REMAINS" -> ProductCategory.HUMAN_REMAINS;
			default -> throw new NonRetryableKafkaProcessingException(
					"Unknown product category: " + wireValue
			);
		};
	}

	public static String toWire(final FragilityLevel fragility) {
		return switch (fragility) {
			case STANDARD -> "STANDARD";
			case FRAGILE -> "FRAGILE";
			case ULTRA_FRAGILE -> "ULTRA_FRAGILE";
		};
	}

	public static FragilityLevel fragilityFromWire(final String wireValue) {
		return switch (wireValue) {
			case "STANDARD" -> FragilityLevel.STANDARD;
			case "FRAGILE" -> FragilityLevel.FRAGILE;
			case "ULTRA_FRAGILE" -> FragilityLevel.ULTRA_FRAGILE;
			default -> throw new NonRetryableKafkaProcessingException(
					"Unknown fragility level: " + wireValue
			);
		};
	}
}
