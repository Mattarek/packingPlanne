package org.example.packing.application.strategy;

import org.example.packing.domain.model.Position;

import java.util.Comparator;

/**
 * Selects the "best" position among candidate extreme points: the minimum
 * according to {@link #comparator()} wins. Injected into
 * {@link ExtremePointPackingStrategy}, like {@link PackageOrdering}, so a
 * different heuristic is a new implementation, not an edit of the strategy.
 */
@FunctionalInterface
public interface PositionScorer {

	Comparator<Position> comparator();
}
