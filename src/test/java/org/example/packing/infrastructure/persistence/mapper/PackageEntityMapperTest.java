package org.example.packing.infrastructure.persistence.mapper;

import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.persistence.entity.PackageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackageEntityMapperTest {

	private PackageEntityMapper mapper;

	private UUID packageId;
	private PackageEntity packageEntity;
	private Package pkg;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(PackageEntityMapper.class);

		packageId = UUID.randomUUID();

		packageEntity = new PackageEntity(
				packageId, 10.0, 20.0, 30.0, 5.5, ProductCategory.FOOD, FragilityLevel.FRAGILE
		);
		pkg = new Package(
				packageId,
				new Dimensions(10.0, 20.0, 30.0),
				new Weight(5.5),
				ProductCategory.FOOD,
				FragilityLevel.FRAGILE
		);
	}

	@Test
	void shouldMapEntityToDomain() {
		// when & then
		assertThat(mapper.toDomain(packageEntity)).isEqualTo(pkg);
	}

	@Test
	void shouldMapDomainToEntityKeepingTheDomainId() {
		// when
		final PackageEntity result = mapper.toEntity(pkg);

		// then
		assertThat(result.getId()).isEqualTo(packageId);
		assertThat(result.getLength()).isEqualTo(10.0);
		assertThat(result.getWidth()).isEqualTo(20.0);
		assertThat(result.getHeight()).isEqualTo(30.0);
		assertThat(result.getWeight()).isEqualTo(5.5);
		assertThat(result.getCategory()).isEqualTo(ProductCategory.FOOD);
		assertThat(result.getFragility()).isEqualTo(FragilityLevel.FRAGILE);
		assertThat(result.isNew()).isTrue();
	}

	@Test
	void shouldMapLists() {
		// when & then
		assertThat(mapper.toDomainList(List.of(packageEntity))).containsExactly(pkg);
		assertThat(mapper.toEntityList(List.of(pkg))).hasSize(1);
	}
}
