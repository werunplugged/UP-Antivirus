pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://unplugged.jfrog.io/artifactory/unplugged-libraries")
            credentials {
                username = System.getenv("ARTIFACTORY_USER")
                password = System.getenv("ARTIFACTORY_PASSWORD")
            }
        }
    }
}

rootProject.name = "UP Antivirus"
include(":app")
include(":hypatia-extensions")
include(":tracker-extension")
include(":signature-scanner")
include(":up-antivirus-common")
include(":up-resources")

include(":tracker-core")
project(":tracker-core").projectDir = File(rootDir, "tracker-extension/tracker-core/")

include(":hypatia-core")
project(":hypatia-core").projectDir = File(rootDir, "hypatia-extensions/hypatia-core/")