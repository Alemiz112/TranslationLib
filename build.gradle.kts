plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

group = "eu.mizerak.alemiz.translationlib"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly("org.projectlombok", "lombok", "1.18.26")
        annotationProcessor("org.projectlombok", "lombok", "1.18.26")
        testCompileOnly("org.projectlombok", "lombok", "1.18.26")
        testAnnotationProcessor("org.projectlombok", "lombok", "1.18.26")

        implementation("org.slf4j", "slf4j-api", "2.0.6")
        implementation("com.google.code.gson:gson:2.10.1")

    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}