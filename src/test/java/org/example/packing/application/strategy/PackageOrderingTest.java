package org.example.packing.application.strategy;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.Weight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackageOrderingTest {

	@Test
	void shouldSortPackagesByVolumeDescending() {
		// given
		final Package small = createPackage(new Dimensions(1.0, 1.0, 1.0), new Weight(10.0));
		final Package medium = createPackage(new Dimensions(2.0, 2.0, 2.0), new Weight(10.0));
		final Package large = createPackage(new Dimensions(3.0, 3.0, 3.0), new Weight(10.0));

		final List<Package> packages = new ArrayList<>(List.of(small, large, medium));

		// when
		packages.sort(PackageOrdering.VOLUME_DESC.comparator());

		// then
		assertThat(packages).containsExactly(large, medium, small);
	}

	@Test
	void shouldSortPackagesByWeightDescending() {
		// given
		final Package light = createPackage(new Dimensions(1.0, 1.0, 1.0), new Weight(10.0));
		final Package medium = createPackage(new Dimensions(1.0, 1.0, 1.0), new Weight(20.0));
		final Package heavy = createPackage(new Dimensions(1.0, 1.0, 1.0), new Weight(30.0));

		final List<Package> packages = new ArrayList<>(List.of(light, heavy, medium));

		// when
		packages.sort(PackageOrdering.WEIGHT_DESC.comparator());

		// then
		assertThat(packages).containsExactly(heavy, medium, light);
	}

	@Test
	void shouldSortPackagesByBaseAreaDescending() {
		// given
		final Package smallBase = createPackage(new Dimensions(2.0, 2.0, 100.0), new Weight(10.0));
		final Package mediumBase = createPackage(new Dimensions(4.0, 5.0, 1.0), new Weight(10.0));
		final Package largeBase = createPackage(new Dimensions(10.0, 10.0, 1.0), new Weight(10.0));

		final List<Package> packages = new ArrayList<>(List.of(smallBase, largeBase, mediumBase));

		// when
		packages.sort(PackageOrdering.BASE_AREA_DESC.comparator());

		// then
		assertThat(packages).containsExactly(largeBase, mediumBase, smallBase);
	}

	@Test
	void shouldSortPackagesByHeightDescending() {
		// given
		final Package low = createPackage(new Dimensions(10.0, 10.0, 1.0), new Weight(10.0));
		final Package medium = createPackage(new Dimensions(10.0, 10.0, 5.0), new Weight(10.0));
		final Package tall = createPackage(new Dimensions(10.0, 10.0, 10.0), new Weight(10.0));

		final List<Package> packages = new ArrayList<>(List.of(low, tall, medium));

		// when
		packages.sort(PackageOrdering.HEIGHT_DESC.comparator());

		// then
		assertThat(packages).containsExactly(tall, medium, low);
	}

	private Package createPackage(final Dimensions dimensions, final Weight weight) {
		return new Package(
				UUID.randomUUID(),
				dimensions,
				weight
		);
	}
}