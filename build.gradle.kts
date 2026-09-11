plugins {
	java
	idea
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
description = "packing"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-liquibase")
	implementation("org.liquibase:liquibase-core")
// Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-kafka
	implementation("org.springframework.boot:spring-boot-starter-kafka")

	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")

	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-kafka:2.0.5")
	testImplementation("org.awaitility:awaitility:4.3.0")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
	java {
		srcDir("src/integrationTest/java")
	}
	resources {
		srcDir("src/integrationTest/resources")
	}

	compileClasspath += sourceSets["main"].output + configurations["testCompileClasspath"]
	runtimeClasspath += output + compileClasspath + configurations["testRuntimeClasspath"]
}
idea {
	module {
		testSources.from(
			integrationTestSourceSet.java.srcDirs,
			integrationTestSourceSet.resources.srcDirs
		)
	}
}

configurations[integrationTestSourceSet.implementationConfigurationName]
	.extendsFrom(configurations["testImplementation"])

configurations[integrationTestSourceSet.runtimeOnlyConfigurationName]
	.extendsFrom(configurations["testRuntimeOnly"])

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
	description = "Runs integration tests."
	group = "verification"

	testClassesDirs = integrationTestSourceSet.output.classesDirs
	classpath = integrationTestSourceSet.runtimeClasspath

	shouldRunAfter(tasks.test)
}

tasks.check {
	dependsOn(tasks.named("integrationTest"))
}