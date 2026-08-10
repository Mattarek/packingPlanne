package org.example.packing.infrastructure.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.packing.application.service.PackageService;
import org.example.packing.infrastructure.kafka.support.KafkaContainerHandler;
import org.example.packing.infrastructure.kafka.support.KafkaPostgresContainersInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("kafka-integration-test")
@ContextConfiguration(initializers = KafkaPostgresContainersInitializer.class)
class PackageCreateRequestConsumerIntegrationTest {

	private static final String TOPIC = "package-create-requests";
	private static final String DLT_TOPIC = TOPIC + ".DLT";

	@MockitoSpyBean
	private PackageService packageService;

	@AfterEach
	void resetSpy() {
		Mockito.reset(packageService);
	}

	@Test
	void shouldCreatePackagesFromKafkaMessage() {
		// given
		final long before = totalPackages();

		// when
		send(validMessage(UUID.randomUUID()));

		// then
		await().untilAsserted(() ->
				assertThat(totalPackages()).isEqualTo(before + 2)
		);
	}

	@Test
	void shouldIgnoreDuplicateEventViaInbox() {
		// given
		final long before = totalPackages();
		final UUID eventId = UUID.randomUUID();
		final String message = validMessage(eventId);

		// when
		send(message);
		send(message);

		// then
		await().untilAsserted(() ->
				assertThat(totalPackages()).isEqualTo(before + 2)
		);

		verify(packageService, Mockito.timeout(5_000).times(1))
				.createPackages(any());
	}

	@Test
	void shouldRetryOnTransientErrorAndEventuallySucceed() {
		// given
		final long before = totalPackages();
		final AtomicInteger invocationCount = new AtomicInteger();
		final UUID eventId = UUID.randomUUID();

		doAnswer(invocation -> {
			if (invocationCount.getAndIncrement() == 0) {
				throw new TransientDataAccessResourceException(
						"Simulated transient database failure"
				);
			}

			return invocation.callRealMethod();
		}).when(packageService).createPackages(any());

		// when
		send(validMessage(eventId));

		// then
		final String retryTopic = discoverRetryTopicName();
		final List<ConsumerRecord<String, String>> retryRecords =
				collectRecords(
						retryTopic,
						1,
						Duration.ofSeconds(30),
						value -> value.contains(eventId.toString())
				);
		assertThat(retryRecords).isNotEmpty();

		await().untilAsserted(() ->
				assertThat(totalPackages()).isEqualTo(before + 2)
		);

		verify(packageService, Mockito.timeout(5_000).times(2))
				.createPackages(any());
	}

	@Test
	void shouldRejectNonRetryableEventDirectlyToDlt() {
		// given
		final long before = totalPackages();
		final UUID eventId = UUID.randomUUID();
		final String message = """
				{
				  "eventId": "%s",
				  "eventType": "UNKNOWN_EVENT_TYPE",
				  "version": 1,
				  "occurredAt": "%s",
				  "packages": [
				    {
				      "length": 10.0,
				      "width": 20.0,
				      "height": 30.0,
				      "weight": 5.5
				    }
				  ]
				}
				""".formatted(eventId, Instant.now());

		// when
		send(message);

		// then
		final List<ConsumerRecord<String, String>> dltRecords =
				collectRecords(
						DLT_TOPIC,
						1,
						Duration.ofSeconds(10),
						value -> value.contains(eventId.toString())
				);

		assertThat(dltRecords).isNotEmpty();
		assertThat(totalPackages()).isEqualTo(before);
	}

	@Test
	void shouldSendMalformedJsonDirectlyToDlt() {
		// given
		final long before = totalPackages();
		final String malformedJson = "{ this is not valid JSON ";

		// when
		send(malformedJson);

		// then
		final List<ConsumerRecord<String, String>> dltRecords =
				collectRecords(
						DLT_TOPIC,
						1,
						Duration.ofSeconds(10),
						malformedJson::equals
				);

		assertThat(dltRecords).isNotEmpty();
		assertThat(totalPackages()).isEqualTo(before);
		verify(packageService, Mockito.timeout(5_000).times(0))
				.createPackages(any());
	}

	@Test
	void shouldRejectUnsupportedEventVersionDirectlyToDlt() {
		// given
		final long before = totalPackages();
		final UUID eventId = UUID.randomUUID();
		final String message = """
				{
				  "eventId": "%s",
				  "eventType": "PACKAGE_CREATE_REQUESTED",
				  "version": 2,
				  "occurredAt": "%s",
				  "packages": [
				    {
				      "length": 10.0,
				      "width": 20.0,
				      "height": 30.0,
				      "weight": 5.5
				    }
				  ]
				}
				""".formatted(eventId, Instant.now());

		// when
		send(message);

		// then
		final List<ConsumerRecord<String, String>> dltRecords =
				collectRecords(
						DLT_TOPIC,
						1,
						Duration.ofSeconds(10),
						value -> value.contains(eventId.toString())
				);

		assertThat(dltRecords).isNotEmpty();
		assertThat(totalPackages()).isEqualTo(before);
	}

	@Test
	void shouldRejectEventFailingBeanValidationDirectlyToDlt() {
		// given
		final long before = totalPackages();
		final UUID eventId = UUID.randomUUID();
		final String message = """
				{
				  "eventId": "%s",
				  "eventType": "PACKAGE_CREATE_REQUESTED",
				  "version": 1,
				  "occurredAt": "%s",
				  "packages": [
				    {
				      "length": -10.0,
				      "width": 20.0,
				      "height": 30.0,
				      "weight": 5.5
				    }
				  ]
				}
				""".formatted(eventId, Instant.now());

		// when
		send(message);

		// then
		final List<ConsumerRecord<String, String>> dltRecords =
				collectRecords(
						DLT_TOPIC,
						1,
						Duration.ofSeconds(10),
						value -> value.contains(eventId.toString())
				);

		assertThat(dltRecords).isNotEmpty();
		assertThat(totalPackages()).isEqualTo(before);
	}

	@Test
	void shouldSendToDltAfterExhaustingRetries() {
		// given
		final long before = totalPackages();
		final UUID eventId = UUID.randomUUID();

		doThrow(new TransientDataAccessResourceException(
				"Simulated persistent transient database failure"
		)).when(packageService).createPackages(any());

		// when
		send(validMessage(eventId));

		// then
		final List<ConsumerRecord<String, String>> dltRecords =
				collectRecords(
						DLT_TOPIC,
						1,
						Duration.ofSeconds(30),
						value -> value.contains(eventId.toString())
				);

		assertThat(dltRecords).isNotEmpty();
		assertThat(totalPackages()).isEqualTo(before);
	}

	private long totalPackages() {
		return packageService.getPackages(0, 1).getTotalElements();
	}

	private String validMessage(final UUID eventId) {
		return """
				{
				  "eventId": "%s",
				  "eventType": "PACKAGE_CREATE_REQUESTED",
				  "version": 1,
				  "occurredAt": "%s",
				  "packages": [
				    {
				      "length": 10.0,
				      "width": 20.0,
				      "height": 30.0,
				      "weight": 5.5
				    },
				    {
				      "length": 15.0,
				      "width": 25.0,
				      "height": 35.0,
				      "weight": 7.2
				    }
				  ]
				}
				""".formatted(eventId, Instant.now());
	}

	private void send(final String message) {
		final KafkaTemplate<String, String> kafkaTemplate = kafkaTemplate();
		kafkaTemplate.send(TOPIC, message);
		kafkaTemplate.flush();
	}

	private KafkaTemplate<String, String> kafkaTemplate() {
		final Map<String, Object> properties = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaContainerHandler.container().getBootstrapServers(),

				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class,

				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class
		);

		return new KafkaTemplate<>(
				new DefaultKafkaProducerFactory<>(properties)
		);
	}

	private String discoverRetryTopicName() {
		final Map<String, Object> adminProps = Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaContainerHandler.container().getBootstrapServers()
		);

		try (final AdminClient adminClient = AdminClient.create(adminProps)) {
			return await()
					.atMost(Duration.ofSeconds(15))
					.until(
							() -> adminClient.listTopics()
									.names()
									.get(10, TimeUnit.SECONDS)
									.stream()
									.filter(name -> name.startsWith(TOPIC)
											&& name.contains("retry"))
									.sorted()
									.findFirst(),
							Optional::isPresent
					)
					.orElseThrow();
		} catch (final Exception exception) {
			throw new IllegalStateException(
					"Retry topic for " + TOPIC + " was not created", exception
			);
		}
	}

	private List<ConsumerRecord<String, String>> collectRecords(
			final String topic,
			final int minCount,
			final Duration timeout,
			final Predicate<String> valueFilter
	) {
		final Map<String, Object> consumerProps = Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaContainerHandler.container().getBootstrapServers(),

				ConsumerConfig.GROUP_ID_CONFIG,
				"test-consumer-" + UUID.randomUUID(),

				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
				"earliest",

				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class,

				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class
		);

		final List<ConsumerRecord<String, String>> collected = new ArrayList<>();

		try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
			consumer.subscribe(List.of(topic));

			await()
					.atMost(timeout)
					.pollInSameThread()
					.untilAsserted(() -> {
						consumer.poll(Duration.ofMillis(200)).forEach(record -> {
							if (valueFilter.test(record.value())) {
								collected.add(record);
							}
						});
						assertThat(collected.size()).isGreaterThanOrEqualTo(minCount);
					});
		}

		return collected;
	}
}
