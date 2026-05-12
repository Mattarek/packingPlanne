package org.example.packing.application.strategy;

import org.example.packing.domain.model.Package;

import java.util.Comparator;

/**
 * Pluggable orderings for packages. The order in which packages are considered
 * has a major impact on the quality of greedy 3D packing heuristics.
 */
public enum PackageOrdering {

	/**
	 * Largest volume first — classic 3D-BPP heuristic, generally strong default.
	 */
	VOLUME_DESC(Comparator.comparingDouble(Package::volume).reversed()),

	/**
	 * Heaviest first — useful when weight is the limiting constraint.
	 */
	WEIGHT_DESC(Comparator.comparingDouble((Package p) -> p.weight().kilograms()).reversed()),

	/**
	 * Largest base footprint first — improves stacking stability.
	 */
	BASE_AREA_DESC(Comparator.comparingDouble(
			(Package p) -> p.dimensions().length() * p.dimensions().width()).reversed()),

	/**
	 * Tallest first — useful for cargo with strict height tiers.
	 */
	HEIGHT_DESC(Comparator.comparingDouble(
			(Package p) -> p.dimensions().height()).reversed());

	private final Comparator<Package> comparator;

	PackageOrdering(final Comparator<Package> comparator) {
		this.comparator = comparator;
	}

	public Comparator<Package> comparator() {
		return comparator;
	}
}
