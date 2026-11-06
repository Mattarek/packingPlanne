package org.example.packing.domain.packing;

import org.example.packing.domain.model.Position;

import java.util.Comparator;

public final class PositionPreferences {

	public static final Comparator<Position> BOTTOM_LEFT_BACK =
			Comparator.comparingDouble(Position::z)
					.thenComparingDouble(Position::y)
					.thenComparingDouble(Position::x);

	private PositionPreferences() {
	}
}
