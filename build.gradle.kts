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
	implementation(kotlin("stdlib"))
}

tasks.withType<KotlinCompile>().all {
	kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
}