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
    }
}

rootProject.name = "SeChat"

include(":app")
include(":core:crypto")
include(":core:data")
include(":core:p2p")
include(":feature:identity")
include(":feature:contacts")
include(":feature:chat")
