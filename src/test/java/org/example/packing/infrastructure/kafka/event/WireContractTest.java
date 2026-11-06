package org.example.packing.infrastructure.kafka.event;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.kafka.exception.NonRetryableKafkaProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WireContractTest {

	@ParameterizedTest
	@EnumSource(ProductCategory.class)
	void shouldMapEveryProductCategoryBothWays(final ProductCategory category) {
		final String wireValue = ProductCategoryWire.fromDomain(category).wireValue();

		assertThat(ProductCategoryWire.fromWire(wireValue).domain()).isEqualTo(category);
	}

	@ParameterizedTest
	@EnumSource(FragilityLevel.class)
	void shouldMapEveryFragilityLevelBothWays(final FragilityLevel fragility) {
		final String wireValue = FragilityLevelWire.fromDomain(fragility).wireValue();

		assertThat(FragilityLevelWire.fromWire(wireValue).domain()).isEqualTo(fragility);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "SPACESHIPS", "food"})
	void shouldRejectUnknownProductCategory(final String wireValue) {
		assertThatThrownBy(() -> ProductCategoryWire.fromWire(wireValue))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unknown product category");
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "BRITTLE", "fragile"})
	void shouldRejectUnknownFragilityLevel(final String wireValue) {
		assertThatThrownBy(() -> FragilityLevelWire.fromWire(wireValue))
				.isInstanceOf(NonRetryableKafkaProcessingException.class)
				.hasMessageContaining("Unknown fragility level");
	}

	@ParameterizedTest
	@EnumSource(EventType.class)
	void shouldResolveEveryEventTypeByItsWireValue(final EventType type) {
		assertThat(EventType.findByWireValue(type.wireValue())).contains(type);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "UNKNOWN_EVENT_TYPE", "package_created"})
	void shouldNotResolveUnknownEventType(final String wireValue) {
		assertThat(EventType.findByWireValue(wireValue)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1})
	void shouldRejectNonPositiveContractVersion(final int version) {
		assertThatThrownBy(() -> new EventContract(EventType.PACKAGE_CREATED, version))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectContractWithoutType() {
		assertThatThrownBy(() -> new EventContract(null, 1))
				.isInstanceOf(NullPointerException.class);
	}
}
