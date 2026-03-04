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
    }
}

rootProject.name = "UP Antivirus"
include(":app")
include(":hypatia-extensions")
include(":tracker-extension")
include(":signature-scanner")
include(":up-antivirus-common")
include(":account-helper")
include(":attestation-helper")
include(":up-resources")

include(":tracker-core")
project(":tracker-core").projectDir = File(rootDir, "tracker-extension/tracker-core/")

include(":hypatia-core")
project(":hypatia-core").projectDir = File(rootDir, "hypatia-extensions/hypatia-core/")