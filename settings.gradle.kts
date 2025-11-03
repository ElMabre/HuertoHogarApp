pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/ElMabre/HuertoHogarApp")

            // Solo intentar configurar credenciales SI existen en un archivo gradle.properties
            // Esto evita que el build falle para usuarios nuevos.
            if (extra.has("gpr.user") && extra.has("gpr.key")) {
                credentials {
                    username = extra["gpr.user"] as String
                    password = extra["gpr.key"] as String
                }
            }

        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    
        maven {
            url = uri("https://maven.pkg.github.com/ElMabre/HuertoHogarApp")
        
            if (extra.has("gpr.user") && extra.has("gpr.key")) {
                credentials {
                    username = extra["gpr.user"] as String
                    password = extra["gpr.key"] as String
                }
            }
        }
    }
}

rootProject.name = "HuertoHogarApp"
include(":app")
