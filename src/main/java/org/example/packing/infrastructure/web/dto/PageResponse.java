package org.example.packing.infrastructure.web.dto;

import org.example.packing.application.port.PageResult;

import java.util.List;

public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last
) {
	public static <T> PageResponse<T> from(final PageResult<T> page) {
		return new PageResponse<>(
				page.content(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				page.isFirst(),
				page.isLast()
		);
	}
}