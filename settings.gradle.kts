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

rootProject.name = "SaglikApp"

include(":app")

include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:healthconnect")

include(":domain")

include(":data:local")
include(":data:repository")

include(":feature:onboarding")
include(":feature:summary")
include(":feature:weight")
include(":feature:bmi")
include(":feature:sleep")
include(":feature:addentry")
include(":feature:profile")
