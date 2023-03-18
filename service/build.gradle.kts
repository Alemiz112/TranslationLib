plugins {
    id("java")
    id("application")
}

group = "eu.mizerak.alemiz.translationlib.service"


dependencies {
    implementation(project(":common"))
    implementation(libs.slf4j.simple)
    implementation(libs.mongo.driver)
    implementation(libs.javalin)
    implementation(libs.avaje.inject)

    annotationProcessor(libs.avaje.inject.generator)
    annotationProcessor(libs.avaje.http.server.generator)
    annotationProcessor(libs.avaje.http.client.generator)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


application {
    mainClass.set("eu.mizerak.alemiz.translationlib.service.TranslationLibService")
}