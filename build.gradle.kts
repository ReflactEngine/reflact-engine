plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
}

group = "net.reflact"
version = "2026.01.08-1.21.11"

repositories {
    mavenCentral()
    // Minestom repository
    maven("https://jitpack.io")
    // maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    // using a recent commit hash for Minestom, ensuring we are up to date
    // Note: User can update this to the specific commit they want
    implementation("com.github.Minestom:Minestom:2026.01.08-1.21.11")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // JSON
    implementation("com.google.code.gson:gson:2.11.0")

    // Database
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
