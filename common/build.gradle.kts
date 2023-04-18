plugins {
    id("java")
    id("maven-publish")
}

description = "Common module"

dependencies {
    api(libs.avaje.http.api)
    api(libs.avaje.http.client)
    api(libs.avaje.http.client.gson)

    annotationProcessor(libs.avaje.http.client.generator)

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.slf4j.simple)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
   dependsOn(tasks.clean)
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components.getByName("java"))
        }
    }

    repositories {
        maven {
            name = "mznt-repo"
            url = uri("https://repo.mznt.eu/snapshots")
            credentials {
                username = System.getenv("DEPLOY_USERNAME")
                password = System.getenv("DEPLOY_PASSWORD")
            }
        }
    }
}