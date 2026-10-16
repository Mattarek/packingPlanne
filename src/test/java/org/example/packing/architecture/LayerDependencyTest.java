package org.example.packing.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Dependency rules from the SOLID review, checked on every build so DIP
 * violations can't slip back in unnoticed:
 * <ul>
 *   <li>the domain depends on nothing but itself and the JDK,</li>
 *   <li>the application depends on the domain and on its own ports – never on
 *       infrastructure, JPA or Kafka.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "org.example.packing", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerDependencyTest {

	@ArchTest
	static final ArchRule domainDependsOnNoOtherLayer = noClasses()
			.that().resideInAPackage("..packing.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..packing.application..",
					"..packing.infrastructure..",
					"..packing.config.."
			)
			.because("the domain is the innermost layer (D1, O1)");

	@ArchTest
	static final ArchRule domainIsFrameworkFree = noClasses()
			.that().resideInAPackage("..packing.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..",
					"jakarta.persistence..",
					"org.apache.kafka.."
			)
			.because("business rules must not depend on frameworks (O1: GlobalExceptionHandler lived in domain)");

	@ArchTest
	static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
			.that().resideInAPackage("..packing.application..")
			.should().dependOnClassesThat().resideInAPackage("..packing.infrastructure..")
			.because("the application talks to infrastructure only through its ports (D1, D2, I1)");

	@ArchTest
	static final ArchRule applicationDoesNotUsePersistenceOrMessagingApis = noClasses()
			.that().resideInAPackage("..packing.application..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"jakarta.persistence..",
					"org.springframework.data.jpa..",
					"org.springframework.kafka..",
					"org.apache.kafka.."
			)
			.because("JPA and Kafka are infrastructure details (D1, S1)");
}
