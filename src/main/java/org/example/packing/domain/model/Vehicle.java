package org.example.packing.domain.model;

import java.util.Objects;

/**
 * Domain entity representing a transport vehicle's cargo area constraints.
 */
public record Vehicle(String id, String name, Dimensions cargoArea, Weight maxPayload) {

	public Vehicle(final String id, final String name, final Dimensions cargoArea, final Weight maxPayload) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.name = Objects.requireNonNull(name, "name must not be null");
		this.cargoArea = Objects.requireNonNull(cargoArea, "cargoArea must not be null");
		this.maxPayload = Objects.requireNonNull(maxPayload, "maxPayload must not be null");
		if (id.isBlank()) {
			throw new IllegalArgumentException("id must not be blank");
		}
	}

	public double cargoVolume() {
		return cargoArea.volume();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Vehicle other)) {
			return false;
		}
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Vehicle[id=%s, %s, cargo=%s, maxPayload=%s]"
				.formatted(id, name, cargoArea, maxPayload);
	}
}
