package org.example.packing.infrastructure.persistence.repository;

import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.example.packing.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PackageRepositoryIT extends AbstractPostgresIntegrationTest {

	private final PackageRepository packageRepository;

	public PackageRepositoryIT(final PackageRepository packageRepository) {
		this.packageRepository = packageRepository;
	}

	@Test
	void shouldSaveAndFindPackageUsingPostgresContainer() {
		// given
		final PackageEntity entity = new PackageEntity(
				null,
				10.0,
				20.0,
				30.0,
				5.5,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		// when
		final PackageEntity saved = packageRepository.saveAndFlush(entity);

		// then
		assertThat(saved.getId()).isNotNull();

		final PackageEntity found = packageRepository.findById(saved.getId())
				.orElseThrow();

		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getLength()).isEqualTo(10.0);
		assertThat(found.getWidth()).isEqualTo(20.0);
		assertThat(found.getHeight()).isEqualTo(30.0);
		assertThat(found.getWeight()).isEqualTo(5.5);
	}

	@Test
	void shouldDeletePackageUsingPostgresContainer() {
		// given
		final PackageEntity entity = new PackageEntity(
				null,
				10.0,
				20.0,
				30.0,
				5.5,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		final PackageEntity saved = packageRepository.saveAndFlush(entity);

		// when
		packageRepository.deleteById(saved.getId());
		packageRepository.flush();

		// then
		assertThat(packageRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void shouldFindAllSavedPackagesUsingPostgresContainer() {
		// given
		final PackageEntity first = new PackageEntity(
				null,
				10.0,
				20.0,
				30.0,
				5.5,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		final PackageEntity second = new PackageEntity(
				null,
				40.0,
				50.0,
				60.0,
				15.0,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		packageRepository.saveAndFlush(first);
		packageRepository.saveAndFlush(second);

		// when
		final var result = packageRepository.findAll();

		// then
		assertThat(result)
				.hasSize(2)
				.extracting(PackageEntity::getWeight)
				.containsExactlyInAnyOrder(5.5, 15.0);
	}

	@Test
	void shouldGenerateDifferentIdsForDifferentPackages() {
		// given
		final PackageEntity first = new PackageEntity(
				null,
				10.0,
				20.0,
				30.0,
				5.5,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		final PackageEntity second = new PackageEntity(
				null,
				40.0,
				50.0,
				60.0,
				15.0,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		// when
		final PackageEntity savedFirst = packageRepository.saveAndFlush(first);
		final PackageEntity savedSecond = packageRepository.saveAndFlush(second);

		// then
		assertThat(savedFirst.getId()).isNotNull();
		assertThat(savedSecond.getId()).isNotNull();
		assertThat(savedFirst.getId()).isNotEqualTo(savedSecond.getId());
	}
}