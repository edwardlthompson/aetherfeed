pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "aetherfeed-desktop-compose"
include(":composeApp")
include(":shared-domain")
project(":shared-domain").projectDir = file("../../../shared/domain")
include(":shared-vault")
project(":shared-vault").projectDir = file("../../../shared/vault")