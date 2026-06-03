package org.example.packing.application.service;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.PackedVehicle;
import org.example.packing.domain.model.PlacedPackage;

import java.util.Objects;

/**
 * Renders a {@link PackingReport} as human-readable text.
 * Separated from the service to honour the Single Responsibility Principle —
 * formatting is a presentation concern.
 */
public final class ReportPrinter {

	private static final String SEPARATOR = "─".repeat(70);

	public String print(final PackingReport report) {
		Objects.requireNonNull(report, "report must not be null");
		final StringBuilder sb = new StringBuilder();
		appendHeader(sb, report);
		appendVehicles(sb, report);
		appendUnpacked(sb, report);
		appendFooter(sb, report);
		return sb.toString();
	}

	private void appendHeader(final StringBuilder sb, final PackingReport r) {
		sb.append(SEPARATOR).append('\n');
		sb.append("PACKING REPORT — strategy: ").append(r.strategyName()).append('\n');
		sb.append(SEPARATOR).append('\n');
		sb.append("Input packages:    ").append(r.totalPackagesIn()).append('\n');
		sb.append("Packed:            ").append(r.getPackedPackagesCount()).append('\n');
		sb.append("Unpacked:          ").append(r.unpackedPackages().size()).append('\n');
		sb.append("Vehicles used:     ").append(r.getVehiclesUsed()).append('\n');
		sb.append("Avg vol. usage:    %.1f%%%n".formatted(r.getAverageVolumeUtilization() * 100));
		sb.append("Avg weight usage:  %.1f%%%n".formatted(r.getAverageWeightUtilization() * 100));
		sb.append("Elapsed:           ").append(r.elapsedMillis()).append(" ms\n");
		sb.append(SEPARATOR).append('\n');
	}

	private void appendVehicles(final StringBuilder sb, final PackingReport r) {
		int vehicleNo = 1;
		for (final PackedVehicle vehicle : r.packedVehicles()) {
			if (vehicle.placedPackages().isEmpty()) {
				continue;
			}
			sb.append("Vehicle #").append(vehicleNo++)
					.append(" — ").append(vehicle.vehicle().name()).append('\n');
			sb.append("  Cargo area:   ").append(vehicle.vehicle().cargoArea()).append('\n');
			sb.append("  Weight:       ")
					.append(vehicle.currentWeight())
					.append(" / ").append(vehicle.vehicle().maxPayload())
					.append("  (%.1f%%)%n".formatted(vehicle.weightUtilization() * 100));
			sb.append("  Volume usage: %.1f%%%n".formatted(vehicle.volumeUtilization() * 100));
			sb.append("  Items: ").append(vehicle.placedPackages().size()).append('\n');
			for (final PlacedPackage pp : vehicle.placedPackages()) {
				sb.append("    • ").append(pp.pkg().id())
						.append("  ").append(pp.pkg().dimensions())
						.append("  ").append(pp.pkg().weight())
						.append("  @ ").append(pp.position())
						.append('\n');
			}
			sb.append('\n');
		}
	}

	private void appendUnpacked(final StringBuilder sb, final PackingReport r) {
		if (r.unpackedPackages().isEmpty()) {
			return;
		}
		sb.append("UNPACKED PACKAGES (").append(r.unpackedPackages().size()).append("):\n");
		for (final Package p : r.unpackedPackages()) {
			sb.append("  ✗ ").append(p).append('\n');
		}
		sb.append('\n');
	}

	private void appendFooter(final StringBuilder sb, final PackingReport r) {
		sb.append(SEPARATOR).append('\n');
		sb.append(r.isFullyPacked()
				? "✓ All packages successfully packed."
				: "⚠ Some packages could not be packed — consider adding vehicles.");
		sb.append('\n').append(SEPARATOR).append('\n');
	}
}
