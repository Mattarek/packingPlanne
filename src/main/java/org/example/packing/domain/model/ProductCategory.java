package org.example.packing.domain.model;

/**
 * Classifies a package by the kind of product it contains.
 * <p>
 * {@link #isTransportable()} encodes company policy on which categories we
 * are willing to carry at all — see
 * {@link org.example.packing.domain.policy.PackageAcceptancePolicy}.
 */
public enum ProductCategory {

	STANDARD(true),
	ELECTRONICS(true),
	CLOTHING(true),
	FOOD(true),
	COSMETICS(true),
	HAZARDOUS_MATERIAL(false),
	FIREARMS_AND_AMMUNITION(false),
	LIVE_ANIMALS(false),
	ILLEGAL_SUBSTANCES(false),
	HUMAN_REMAINS(false);

	private final boolean transportable;

	ProductCategory(final boolean transportable) {
		this.transportable = transportable;
	}

	/**
	 * Whether the company transports products of this category at all,
	 * independent of the specific package's size, weight or fragility.
	 */
	public boolean isTransportable() {
		return transportable;
	}
}
