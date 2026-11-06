package org.example.packing.infrastructure.serialization;

import org.example.packing.infrastructure.messaging.PayloadDeserializationException;
import org.example.packing.infrastructure.messaging.PayloadDeserializer;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class JacksonPayloadDeserializer implements PayloadDeserializer {

	private final ObjectMapper objectMapper;

	public JacksonPayloadDeserializer(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public <T> T deserialize(final String payload, final Class<T> type) {
		try {
			return objectMapper.readValue(payload, type);
		} catch (final JacksonException exception) {
			throw new PayloadDeserializationException(
					"Failed to deserialize " + type.getSimpleName(),
					exception
			);
		}
	}
}
