pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
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
