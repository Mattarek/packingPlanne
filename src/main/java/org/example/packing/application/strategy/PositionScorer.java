package org.example.packing.application.strategy;

import org.example.packing.domain.model.Position;

import java.util.Comparator;

/**
 * Selects the "best" position among candidate extreme points.
 * <p>
 * Default heuristic: <b>bottom-left-back fill</b> — prefer the lowest Z (don't stack
 * if you can avoid it), then smallest Y (push to back), then smallest X (push to left).
 * This produces stable, predictable, dense packings.
 */
final class PositionScorer {

	private static final Comparator<Position> BOTTOM_LEFT_BACK =
			Comparator.comparingDouble(Position::z)
					.thenComparingDouble(Position::y)
					.thenComparingDouble(Position::x);

	Comparator<Position> comparator() {
		return BOTTOM_LEFT_BACK;
	}
}
