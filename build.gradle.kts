import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.3.50"
}

group = "de.earley"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	api(kotlin("stdlib"))

	testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.6")
	testImplementation("io.kotest:kotest-assertions-core-jvm:4.0.6")
}

tasks.withType<KotlinCompile>().all {
	kotlinOptions {
		jvmTarget = "1.8"
		freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}