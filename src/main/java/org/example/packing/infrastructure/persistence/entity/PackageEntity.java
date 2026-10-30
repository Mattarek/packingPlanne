package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.ProductCategory;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "packages")
public class PackageEntity implements Persistable<UUID> {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Transient
	private boolean isNew = true;

	private double length;
	private double width;
	private double height;

	private double weight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FragilityLevel fragility;

	protected PackageEntity() {
	}

	public PackageEntity(
			final UUID id,
			final double length,
			final double width,
			final double height,
			final double weight,
			final ProductCategory category,
			final FragilityLevel fragility
	) {
		this.id = id;
		this.length = length;
		this.width = width;
		this.height = height;
		this.weight = weight;
		this.category = category;
		this.fragility = fragility;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostLoad
	@PostPersist
	void markNotNew() {
		isNew = false;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getWeight() {
		return weight;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public FragilityLevel getFragility() {
		return fragility;
	}
}
