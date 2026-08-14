package org.example.packing.domain.policy;

import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;

import java.util.Arrays;
import java.util.Objects;

/**
 * Company-wide rules applied to every package at intake, independent of which
 * vehicle (if any) it will eventually be loaded onto.
 * <p>
 * We do not accept packages that:
 * <ul>
 *   <li>exceed {@link #MAX_ACCEPTED_DIMENSIONS} in any orientation,</li>
 *   <li>exceed {@link #MAX_ACCEPTED_WEIGHT},</li>
 *   <li>belong to a {@link ProductCategory} we don't transport, or</li>
 *   <li>are {@link FragilityLevel#ULTRA_FRAGILE}.</li>
 * </ul>
 * A package failing any of these checks is rejected before it is ever
 * persisted — see {@link org.example.packing.application.service.PackageService}.
 * This is a separate concern from
 * {@link org.example.packing.domain.exception.PackageTooLargeException}, which
 * checks fit against a specific vehicle during packing.
 */
public final class PackageAcceptancePolicy {

	/** Largest dimensions we accept, checked orientation-independently (centimeters). */
	public static final Dimensions MAX_ACCEPTED_DIMENSIONS = new Dimensions(120.0, 80.0, 80.0);

	/** Heaviest package we accept (kilograms). */
	public static final Weight MAX_ACCEPTED_WEIGHT = new Weight(30.0);

	private PackageAcceptancePolicy() {
	}

	/**
	 * @throws PackageNotAcceptedException if the package violates any acceptance rule
	 */
	public static void validate(
			final Dimensions dimensions,
			final Weight weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		Objects.requireNonNull(dimensions, "dimensions must not be null");
		Objects.requireNonNull(weight, "weight must not be null");
		Objects.requireNonNull(category, "category must not be null");
		Objects.requireNonNull(fragility, "fragility must not be null");

		if (!fitsWithinMaxDimensions(dimensions)) {
			throw new PackageNotAcceptedException(
					"dimensions %s exceed the maximum accepted dimensions %s"
							.formatted(dimensions, MAX_ACCEPTED_DIMENSIONS));
		}

		if (!weight.isLessThanOrEqualTo(MAX_ACCEPTED_WEIGHT)) {
			throw new PackageNotAcceptedException(
					"weight %s exceeds the maximum accepted weight %s"
							.formatted(weight, MAX_ACCEPTED_WEIGHT));
		}

		if (!category.isTransportable()) {
			throw new PackageNotAcceptedException(
					"product category %s is not transported".formatted(category));
		}

		if (fragility == FragilityLevel.ULTRA_FRAGILE) {
			throw new PackageNotAcceptedException("ultra-fragile packages are not transported");
		}
	}

	/**
	 * Compares the package's dimensions against {@link #MAX_ACCEPTED_DIMENSIONS}
	 * sorted ascending on both sides, so a package can be rotated freely to fit
	 * within the accepted envelope (unlike {@link Dimensions#fitsInside}, which
	 * is axis-aligned and used for fitting a specific vehicle's cargo area).
	 */
	private static boolean fitsWithinMaxDimensions(final Dimensions dimensions) {
		final double[] actual = sortedAscending(dimensions);
		final double[] max = sortedAscending(MAX_ACCEPTED_DIMENSIONS);

		return actual[0] <= max[0] && actual[1] <= max[1] && actual[2] <= max[2];
	}

	private static double[] sortedAscending(final Dimensions dimensions) {
		final double[] values = {dimensions.length(), dimensions.width(), dimensions.height()};
		Arrays.sort(values);
		return values;
	}
}
