plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.zamolski.storagereplicator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.google.cloud:libraries-bom:26.24.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.google.cloud:google-cloudevent-types:0.14.0")
    compileOnly("com.google.cloud.functions:functions-framework-api:1.1.0")
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
