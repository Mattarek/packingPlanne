package org.example.packing.application.strategy;

import org.example.packing.domain.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PositionScorerTest {

	private PositionScorer positionScorer;

	@BeforeEach
	void setUp() {
		positionScorer = new PositionScorer();
	}

	@Test
	void shouldSortPositionsByZThenYThenX() {
		// given
		final Position highPosition = new Position(0.0, 0.0, 5.0);
		final Position middleYPosition = new Position(0.0, 2.0, 0.0);
		final Position rightPosition = new Position(3.0, 0.0, 0.0);
		final Position bestPosition = new Position(0.0, 0.0, 0.0);

		final List<Position> positions = new ArrayList<>(
				List.of(
						highPosition,
						middleYPosition,
						rightPosition,
						bestPosition
				)
		);

		// when
		positions.sort(positionScorer.comparator());

		// then
		assertThat(positions).containsExactly(
				bestPosition,
				rightPosition,
				middleYPosition,
				highPosition
		);
	}

	@Test
	void shouldPreferLowerZBeforeLowerYAndX() {
		// given
		final Comparator<Position> comparator = positionScorer.comparator();

		final Position lowerZButHigherYAndX = new Position(100.0, 100.0, 1.0);
		final Position higherZButLowerYAndX = new Position(0.0, 0.0, 2.0);

		// when
		final int result = comparator.compare(lowerZButHigherYAndX, higherZButLowerYAndX);

		// then
		assertThat(result).isNegative();
	}

	@Test
	void shouldPreferLowerYWhenZIsEqual() {
		// given
		final Comparator<Position> comparator = positionScorer.comparator();

		final Position lowerY = new Position(100.0, 1.0, 0.0);
		final Position higherY = new Position(0.0, 2.0, 0.0);

		// when
		final int result = comparator.compare(lowerY, higherY);

		// then
		assertThat(result).isNegative();
	}

	@Test
	void shouldPreferLowerXWhenZAndYAreEqual() {
		// given
		final Comparator<Position> comparator = positionScorer.comparator();

		final Position lowerX = new Position(1.0, 0.0, 0.0);
		final Position higherX = new Position(2.0, 0.0, 0.0);

		// when
		final int result = comparator.compare(lowerX, higherX);

		// then
		assertThat(result).isNegative();
	}
}