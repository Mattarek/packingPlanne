package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicules")
public class VehicleEntity {

	@Id
	private String id;

	private String name;

	private double lengthCm;
	private double widthCm;
	private double heightCm;

	private double maxPayloadKg;

	protected VehicleEntity() {
	}

	public VehicleEntity(
			final String id,
			final String name,
			final double lengthCm,
			final double widthCm,
			final double heightCm,
			final double maxPayloadKg
	) {
		this.id = id;
		this.name = name;
		this.lengthCm = lengthCm;
		this.widthCm = widthCm;
		this.heightCm = heightCm;
		this.maxPayloadKg = maxPayloadKg;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public double getMaxPayloadKg() {
		return maxPayloadKg;
	}
}
