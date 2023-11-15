pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        jcenter() {
            content {
                includeModule("com.theartofdev.edmodo", "android-image-cropper")
            }
        }

    }
}

rootProject.name = "Qsee"
include(":app")
