package org.example.packing.application.port;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

	public PageResult {
		content = List.copyOf(content);
	}

	public int totalPages() {
		return (int) Math.ceil((double) totalElements / size);
	}

	public boolean isFirst() {
		return page == 0;
	}

	public boolean isLast() {
		return page + 1 >= totalPages();
	}

	public <R> PageResult<R> map(final Function<? super T, ? extends R> mapper) {
		return new PageResult<>(content.stream().<R>map(mapper).toList(), page, size, totalElements);
	}
}
