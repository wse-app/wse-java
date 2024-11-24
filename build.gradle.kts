plugins {
    id("java-library")
    id("maven-publish")
    signing
}

group = "com.javxa.wse-app"
version = System.getenv()["VERSION"] ?: "0.0.1"

java {
//    withJavadocJar()
//    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// https://www.jetbrains.com/help/idea/add-a-gradle-library-to-the-maven-repository.html
// https://central.sonatype.com/publishing

publishing {
    publications {


        create<MavenPublication>("WseApp") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "Custom"
            url = uri(layout.buildDirectory.dir("custom-maven"))
        }
    }
}

//Javadocs must be provided but not found in entries
//Missing signature for file: wse-java-0.0.1.jar
//Missing signature for file: wse-java-0.0.1.module
//Missing signature for file: wse-java-0.0.1.pom
//Sources must be provided but not found in entries
//Developers information is missing
//License information is missing
//Project URL is not defined
//Project description is missing
//SCM URL is not defined
