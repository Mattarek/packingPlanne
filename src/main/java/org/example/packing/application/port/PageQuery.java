package org.example.packing.application.port;

public record PageQuery(int page, int size) {

	public PageQuery {
		if (page < 0) {
			throw new IllegalArgumentException("page must not be negative");
		}
		if (size < 1) {
			throw new IllegalArgumentException("size must be at least 1");
		}
	}
}
