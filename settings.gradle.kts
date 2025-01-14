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
include(":up-account-debug")
include(":up-account-release")
