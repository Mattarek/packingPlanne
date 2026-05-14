package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "packages")
public class PackageEntity {

	@Id
	private String id;

	private double lengthCm;
	private double widthCm;
	private double heightCm;

	private double weightKg;

	protected PackageEntity() {
	}

	public PackageEntity(
			final String id,
			final double lengthCm,
			final double widthCm,
			final double heightCm,
			final double weightKg
	) {
		this.id = id;
		this.lengthCm = lengthCm;
		this.widthCm = widthCm;
		this.heightCm = heightCm;
		this.weightKg = weightKg;
	}

	public String getId() {
		return id;
	}

	public double getLengthCm() {
		return lengthCm;
	}

	public double getWidthCm() {
		return widthCm;
	}

	public double getHeightCm() {
		return heightCm;
	}

	public double getWeightKg() {
		return weightKg;
	}
}
