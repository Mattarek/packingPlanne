package org.example.packing.application.port;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResultTest {

	@Test
	void shouldComputePagingFlags() {
		// given
		final PageResult<String> middle = new PageResult<>(List.of("c", "d"), 1, 2, 5);

		// when & then
		assertThat(middle.totalPages()).isEqualTo(3);
		assertThat(middle.isFirst()).isFalse();
		assertThat(middle.isLast()).isFalse();
	}

	@Test
	void shouldTreatEmptyResultAsFirstAndLastPage() {
		// given
		final PageResult<String> empty = new PageResult<>(List.of(), 0, 20, 0);

		// when & then
		assertThat(empty.totalPages()).isZero();
		assertThat(empty.isFirst()).isTrue();
		assertThat(empty.isLast()).isTrue();
	}

	@Test
	void shouldMapContentKeepingPagingInfo() {
		// given
		final PageResult<Integer> numbers = new PageResult<>(List.of(1, 2), 0, 2, 4);

		// when
		final PageResult<String> mapped = numbers.map(String::valueOf);

		// then
		assertThat(mapped).isEqualTo(new PageResult<>(List.of("1", "2"), 0, 2, 4));
	}

	@Test
	void shouldRejectInvalidQuery() {
		// when & then
		assertThatThrownBy(() -> new PageQuery(-1, 10)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new PageQuery(0, 0)).isInstanceOf(IllegalArgumentException.class);
	}
}
