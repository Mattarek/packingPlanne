package org.example.packing.application.strategy;

import org.example.packing.domain.model.Position;

import java.util.Comparator;

public final class BottomLeftBackPositionScorer implements PositionScorer {

	private static final Comparator<Position> BOTTOM_LEFT_BACK =
			Comparator.comparingDouble(Position::z)
					.thenComparingDouble(Position::y)
					.thenComparingDouble(Position::x);

	@Override
	public Comparator<Position> comparator() {
		return BOTTOM_LEFT_BACK;
	}
}
