plugins {
    `maven-publish`
    kotlin("jvm") version "2.0.21"
}

group = "ru.kosti"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation(kotlin("test"))
}

publishing {
    repositories {
        maven {
            val githubUsername = System.getenv("USERNAME")
            val githubRepository = System.getenv("REPOSITORY")
            println(githubUsername)
            println(githubRepository)
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/$githubRepository")
            credentials {
                username = githubUsername
                password = System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            artifactId = "com.exposed.pagination"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}