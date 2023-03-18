plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "eu.mizerak.alemiz.translationlib"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(rootProject.libs.lombok)
        implementation(rootProject.libs.slf4j.api)
        implementation(rootProject.libs.gson)

        annotationProcessor(rootProject.libs.lombok)

        // Tests
        testCompileOnly(rootProject.libs.lombok)
        testAnnotationProcessor(rootProject.libs.lombok)
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}