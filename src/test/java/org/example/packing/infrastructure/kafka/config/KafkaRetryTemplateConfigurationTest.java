package org.example.packing.infrastructure.kafka.config;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the actual serialization behavior the bean is built for — see the
 * class Javadoc on {@link KafkaRetryTemplateConfiguration} for why a single
 * template needs to handle several value types.
 */
class KafkaRetryTemplateConfigurationTest {

	private final KafkaRetryTemplateConfiguration configuration = new KafkaRetryTemplateConfiguration();

	@Test
	void shouldSerializeStringValueAsPlainUtf8() {
		final byte[] result = valueSerializer().serialize("some-topic", "hello");

		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("hello");
	}

	@Test
	void shouldSerializePackageCreateRequestEventAsJson() {
		final PackageCreateRequestEvent event = new PackageCreateRequestEvent(
				UUID.randomUUID(), "PACKAGE_CREATE_REQUESTED", 1, Instant.now(), List.of()
		);

		final byte[] result = valueSerializer().serialize("some-topic", event);

		assertThat(new String(result, StandardCharsets.UTF_8))
				.contains(event.eventId().toString())
				.contains("PACKAGE_CREATE_REQUESTED");
	}

	@Test
	void shouldSerializeDeserializationExceptionByReturningOriginalRawBytes() {
		final byte[] originalBytes = "{ not valid json".getBytes(StandardCharsets.UTF_8);
		final DeserializationException exception = new DeserializationException(
				"failed to deserialize", originalBytes, false, new RuntimeException("boom")
		);

		final byte[] result = valueSerializer().serialize("some-topic", exception);

		assertThat(result).isEqualTo(originalBytes);
	}

	@Test
	void shouldSerializeRawByteArrayUnchanged() {
		final byte[] raw = {1, 2, 3, 4};

		final byte[] result = valueSerializer().serialize("some-topic", raw);

		assertThat(result).isEqualTo(raw);
	}

	@Test
	void shouldThrowSerializationExceptionForUnmappedValueType() {
		assertThatThrownBy(() -> valueSerializer().serialize("some-topic", 42))
				.isInstanceOf(SerializationException.class);
	}

	@Test
	void shouldSerializeKeyAsPlainUtf8String() {
		final byte[] result = keySerializer().serialize("some-topic", "my-key");

		assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("my-key");
	}

	@SuppressWarnings("unchecked")
	private Serializer<Object> valueSerializer() {
		return ((DefaultKafkaProducerFactory<String, Object>) producerFactory()).getValueSerializer();
	}

	@SuppressWarnings("unchecked")
	private Serializer<String> keySerializer() {
		return ((DefaultKafkaProducerFactory<String, Object>) producerFactory()).getKeySerializer();
	}

	private ProducerFactory<String, Object> producerFactory() {
		final KafkaTemplate<String, Object> template = configuration.kafkaTemplate(new KafkaProperties());
		return template.getProducerFactory();
	}
}
