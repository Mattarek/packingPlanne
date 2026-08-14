package org.example.packing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class PackingApplication {
	static void main(final String[] args) {
		SpringApplication.run(PackingApplication.class, args);
	}
}

//1. jak wstrzyknac pole z konfiguracji
//	# value
//	public TransferService(
//			@Value("${bank.transfer.max-amount}") BigDecimal maxAmount // constructor injection to dobra praktyka
//	) {
//		this.maxAmount = maxAmount;
//	}

//	# klasa konfiguracyjna ktora jest beanem i my tego beana mozemy wstrzyknac do innych beanow
//		- przy wiekszej liczbie powiazanych wlasciwosci lepiej uzyc @ConfigurationProperties
//			bank.transfer.max-amount=10000
//			bank.transfer.currency=PLN
// 		@ConfigurationProperties(prefix = "bank.transfer")
//		public record TransferProperties(
//			BigDecimal maxAmount,
// 			String currenct
// 		) {}
// i zarejestrować ją np.:
//@Configuration
//@EnableConfigurationProperties(TransferProperties.class)
//public class TransferConfiguration {
//}

// Teraz TransferProperties jest Beanem Springa i możemy go normalnie wstrzyknąć:
//@Service
//public class TransferService {
//
//
//    private final TransferProperties properties;
//
//
//    public TransferService(TransferProperties properties) {
//        this.properties = properties;
//    }
//}

// 2. czy w klasie potomnej mozemy uzyc metody z klasy abstrakcujnej
// 	- tak, przyklad
//public abstract class PaymentService {
//
//	protected void validateAmount(BigDecimal amount) {
//		if (amount.signum() <= 0) {
//			throw new IllegalArgumentException("Amount must be positive");
//		}
//	}
//}

//public class CardPaymentService extends PaymentService {
//
//	public void pay(BigDecimal amount) {
//		validateAmount(amount);
//
//		System.out.println("Payment completed");
//	}
//}

// 3. Jak stworzyc bean w Configuration i czy moga byc publiczne czy nie.