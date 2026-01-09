plugins {
    `java-library`
}

group = "net.reflect"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Minestom repository
    maven("https://jitpack.io")
}

dependencies {
    // using a recent commit hash for Minestom, ensuring we are up to date
    // Note: User can update this to the specific commit they want
    implementation("com.github.Minestom:Minestom:2026.01.08-1.21.11")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
