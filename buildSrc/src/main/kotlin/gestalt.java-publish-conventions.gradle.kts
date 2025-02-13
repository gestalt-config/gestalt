import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

/*
 * Apply the plugin to publish to maven central
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */

plugins {
    `maven-publish`
    signing
    id("com.vanniktech.maven.publish")
}

val publicationName = "gestalt"

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomUrl = "https://github.com/gestalt-config/gestalt"
val pomIssueUrl = "https://github.com/gestalt-config/gestalt/issues"

// to publish locally .\gradlew publishToMavenLocal
// to upload to maven central use: .\gradlew publishAllPublicationsToOssStagingRepository

// to publish to maven central ./gradlew publishToMavenCentral
/*
publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])

            pom {
                name.set("gestalt")
                description.set("A Java Configuration Library")
                url.set(pomUrl)
                licenses {
                    license {
                        name.set("The Apache 2.0 License")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("credmond-git")
                        name.set("Colin Redmond")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/gestalt-config/gestalt")
                    url.set(pomUrl)
                    developerConnection.set("scm:git:https://github.com/credmond-git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "ossStaging"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username =
                    if (project.hasProperty("ossrhUsername")) project.property("ossrhUsername") as String else System.getenv("OSSRH_USERNAME")
                password =
                    if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword") as String else System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications[publicationName])
}
 */

mavenPublishing {
    //publishToMavenCentral(SonatypeHost.DEFAULT)
    // or when publishing to https://s01.oss.sonatype.org
    //publishToMavenCentral(SonatypeHost.S01)
    // or when publishing to https://central.sonatype.com/
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)

    coordinates(artifactGroup, artifactName, artifactVersion)

    configure(JavaLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        javadocJar = JavadocJar.Javadoc(),
        // whether to publish a sources jar
        sourcesJar = true,
    ))

    signAllPublications()
    pom {
        name.set("gestalt")
        description.set("A Java Configuration Library")
        inceptionYear.set("2020")
        url.set(pomUrl)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("credmond-git")
                name.set("Colin Redmond")
            }
        }
        scm {
            url.set(pomUrl)
            connection.set("scm:git:https://github.com/gestalt-config/gestalt")
            developerConnection.set("scm:git:https://github.com/credmond-git")
        }
    }
}

