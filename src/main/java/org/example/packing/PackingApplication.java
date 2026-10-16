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

// - COPY vs ADD w Dockerfile
// RUN - doinstalowujesz liby
// COPY Tylko na adresy
// ADD adresy i pliki, niebezpieczniejsze

// 1. RUN vs ENTRYPOINT
// RUN -
// ENTRYPOINT - w momencie zdefiniowania polecenia, trudno jej nadpisac
// entryoint i cmd wchodzi w interakcje,

// 2. jakie proxxy jeszcze mamy,
// warstwa proxy na polu relacyjnym podczas wywolywania gettera na kolekcji

// 3. n + 1
// 4. kazdy poziom propagacji

// 5. jak zabezpieczyc przez transakcyjnosc
// 6.

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

// 4. waiting lock - liquibase, sprawdzic tabele z lockami w bazie danych
//
// 5.
// 1.
// Partycje logiczne w postgresie

// 2. Na co zwrócić uwage, SOLID, wzorce projektowe, Coupling, Cohesion
// Coupling - co to zla/dobra
// Cohesion - co to zla/dobra
