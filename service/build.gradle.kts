plugins {
    id("java")
    id("application")
}

group = "eu.mizerak.alemiz.translationlib.service"


dependencies {
    implementation(project(":common"))
    // Simple logger
    implementation("org.slf4j:slf4j-simple:2.0.6")
    // Http server
    implementation("io.javalin:javalin:5.4.2")
    // MongoDB drivers
    implementation("org.mongodb", "mongodb-driver-sync", "4.7.0")
    // Web client
    implementation("com.squareup.okhttp3", "okhttp", "4.9.1")
    // Avaje inject
    implementation("io.avaje", "avaje-inject","8.10")
    implementation("io.avaje:avaje-http-api:1.30");
    annotationProcessor("io.avaje", "avaje-inject-generator", "8.10")
    annotationProcessor("io.avaje:avaje-http-javalin-generator:1.30")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


application {
    mainClass.set("eu.mizerak.alemiz.translationlib.service.TranslationLibService")
}