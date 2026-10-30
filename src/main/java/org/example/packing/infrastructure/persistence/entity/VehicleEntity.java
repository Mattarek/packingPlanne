package org.example.packing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class VehicleEntity implements Persistable<UUID> {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Transient
	private boolean isNew = true;

	private String name;

	private double length;
	private double width;
	private double height;

	private double maxPayload;

	protected VehicleEntity() {
	}

	public VehicleEntity(
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
