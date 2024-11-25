plugins {
    id("java-library")
    id("maven-publish")
    signing
}

group = "com.javxa.wse-app"
version = System.getenv()["VERSION"] ?: "0.0.1"

java {
    withJavadocJar()
    withSourcesJar()
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
    repositories {
        maven {
            name = "Custom"
            url = uri(layout.buildDirectory.dir("custom-maven"))
        }
    }

    publications {
        create<MavenPublication>("WseApp") {
            from(components["java"])

            pom {
                url = "https://wse.app"
                description = "Code dependency for java code generated from wse.app"

                scm {
                    url = "https://github.com/wse-app/wse-java"
                    connection = "scm:git:git://github.com/wse-app/wse-java.git"
                    developerConnection = "scm:git:ssh://git@github.com:wse-app/wse-java.git"
                }

                developers {
                    developer {
                        name = "Carl Caesar"
                        organization = "Web Service Engine Sweden AB"
                        email = "carljfcaesar@gmail.com"
                        roles.add("Lead Software Developer")
                        timezone = "CEST"
                    }
                }

                licenses {
                    license {
                        name = "Web Service Engine - Software License"
                        url = "https://wse.app/Web%20Service%20Engine%20-%20Software%20License.pdf"
                        distribution = "global"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["WseApp"])
}