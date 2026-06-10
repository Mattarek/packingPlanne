package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class VehiclesEntity {

	@Id
	private UUID id;

	private String name;

	private double length;
	private double width;
	private double height;

	private double maxPayload;

	protected VehiclesEntity() {
	}

	public VehiclesEntity(
			final UUID id,
			final String name,
			final double length,
			final double width,
			final double height,
			final double maxPayload
	) {
		this.id = id;
		this.name = name;
		this.length = length;
		this.width = width;
		this.height = height;
		this.maxPayload = maxPayload;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public double getMaxPayload() {
		return maxPayload;
	}
}
