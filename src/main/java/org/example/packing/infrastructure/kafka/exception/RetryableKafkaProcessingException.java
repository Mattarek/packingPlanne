package org.example.packing.infrastructure.kafka.exception;

public class RetryableKafkaProcessingException
		extends RuntimeException {

	public RetryableKafkaProcessingException(
			final String message
	) {
		super(message);
	}

	public RetryableKafkaProcessingException(
			final String message,
			final Throwable cause
	) {
		super(message, cause);
	}

	public void test() {
		final int score = 85;

		final String grade = switch (score / 10) {
			case 10, 9 -> "A";
			case 8 -> {
				System.out.println("Bardzo dobry wynik!");
				yield "B";
			}
			case 7 -> "C";
			default -> "F";
		};

		System.out.println(grade);
	}
}