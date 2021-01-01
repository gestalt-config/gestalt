/*
 * Apply the plugin to publish to bintray
 */

plugins {
    `maven-publish`
    id("com.jfrog.bintray")
}

val publicationName = "gestalt"

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomUrl = "https://github.com/credmond-git/gestalt"
val pomScmUrl = "https://github.com/credmond-git/gestalt"
val pomIssueUrl = "https://github.com/credmond-git/gestalt/issues"
val pomName = "gestalt"
val pomDesc = "A Java Confiugration Library"

val githubReadme = "README.md"

val pomLicenseName = "Apache-2.0"
val pomLicenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val pomLicenseDist = "repo"

val pomDeveloperId = "credmond-git"
val pomDeveloperName = "Colin Redmond"


publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])

            pom {
                name.set(pomName)
                description.set(pomDesc)
                url.set(pomUrl)
                licenses {
                    license {
                        name.set(pomLicenseName)
                        url.set(pomLicenseUrl)
                        distribution.set(pomLicenseDist)
                    }
                }
                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                    }
                }
                scm {
                    url.set(pomScmUrl)
                }
            }
        }
    }
}

// to run ./gradlew build bintrayUpload
bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")

    dryRun = false
    publish = true

    setPublications(publicationName)

    pkg.apply {
        repo = publicationName
        name = artifactName
        userOrg = user
        githubRepo = pomScmUrl
        vcsUrl = pomScmUrl
        description = pomDesc
        setLicenses(pomLicenseName)
        desc = description
        websiteUrl = pomUrl
        issueTrackerUrl = pomIssueUrl
        githubReleaseNotesFile = githubReadme

        version.apply {
            name = artifactVersion
            desc = pomDesc
        }
    }
}
