package org.example.packing.infrastructure.web.dto;

import java.util.UUID;

public record DeleteResponse(
		UUID deletedId
) {
}
