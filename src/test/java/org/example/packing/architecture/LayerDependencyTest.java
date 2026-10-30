package org.example.packing.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.example.packing", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerDependencyTest {

	@ArchTest
	static final ArchRule domainDependsOnNoOtherLayer = noClasses()
			.that().resideInAPackage("..packing.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..packing.application..",
					"..packing.infrastructure.."
			)
			.because("the domain is the innermost layer (D1, O1)");

	@ArchTest
	static final ArchRule domainIsFrameworkFree = noClasses()
			.that().resideInAPackage("..packing.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..",
					"jakarta..",
					"javax..",
					"org.hibernate..",
					"org.apache.kafka..",
					"org.mapstruct..",
					"com.fasterxml.jackson..",
					"tools.jackson..",
					"org.slf4j.."
			)
			.because("business rules must not depend on frameworks (O1, A3)");

	@ArchTest
	static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
			.that().resideInAPackage("..packing.application..")
			.should().dependOnClassesThat().resideInAPackage("..packing.infrastructure..")
			.because("the application talks to infrastructure only through its ports (D1, D2, I1)");

	@ArchTest
	static final ArchRule applicationUsesOnlyDependencyInjectionAndTransactions = noClasses()
			.that().resideInAPackage("..packing.application..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"jakarta.persistence..",
					"jakarta.servlet..",
					"jakarta.validation..",
					"org.springframework.data..",
					"org.springframework.web..",
					"org.springframework.http..",
					"org.springframework.kafka..",
					"org.hibernate..",
					"org.apache.kafka..",
					"org.mapstruct..",
					"com.fasterxml.jackson..",
					"tools.jackson.."
			)
			.because("web, persistence, messaging and mapping are infrastructure details (A1, D1, S1)");

	@ArchTest
	static final ArchRule inboundAdaptersUseInboundPorts = noClasses()
			.that().resideInAnyPackage(
					"..packing.infrastructure.web..",
					"..packing.infrastructure.kafka.."
			)
			.should().dependOnClassesThat().resideInAPackage("..packing.application.service..")
			.because("both sides of the hexagon go through ports (A5)");

	@ArchTest
	static final ArchRule messagingIsTechnologyAgnostic = noClasses()
			.that().resideInAPackage("..packing.infrastructure.messaging..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"jakarta.persistence..",
					"org.hibernate..",
					"org.springframework.data..",
					"org.springframework.kafka..",
					"org.apache.kafka..",
					"tools.jackson..",
					"..packing.infrastructure.persistence..",
					"..packing.infrastructure.kafka..",
					"..packing.infrastructure.serialization.."
			)
			.because("outbox, inbox and dead-letter logic depends only on its own ports");
}
