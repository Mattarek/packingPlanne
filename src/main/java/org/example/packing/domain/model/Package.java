package org.example.packing.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Package(UUID id, Dimensions dimensions, Weight weight, ProductCategory category,
                      FragilityLevel fragility) {

	public Package(
			final UUID id,
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.dimensions = Objects.requireNonNull(dimensions, "dimensions must not be null");
		this.weight = Objects.requireNonNull(weight, "weight must not be null");
		this.category = Objects.requireNonNull(category, "category must not be null");
		this.fragility = Objects.requireNonNull(fragility, "fragility must not be null");
	}

	public Package(final UUID id, final Dimensions dimensions, final Weight weight) {
		this(id, dimensions, weight, ProductCategory.STANDARD, FragilityLevel.STANDARD);
	}

	public double volume() {
		return dimensions.volume();
	}
}
