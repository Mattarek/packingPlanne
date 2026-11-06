package org.example.packing.infrastructure.serialization;

import org.example.packing.infrastructure.messaging.PayloadSerializationException;
import org.example.packing.infrastructure.messaging.PayloadSerializer;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class JacksonPayloadSerializer implements PayloadSerializer {

	private final ObjectMapper objectMapper;

	public JacksonPayloadSerializer(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String serialize(final Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (final JacksonException exception) {
			throw new PayloadSerializationException(
					"Failed to serialize " + payload.getClass().getSimpleName(),
					exception
			);
		}
	}
}
