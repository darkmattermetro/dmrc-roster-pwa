plugins {
    kotlin("js") version "1.9.20"
}

group = "com.dmrc.roster"
version = "1.0.0"

kotlin {
    js(IR) {
        browser {
            // Disable tests to avoid Gradle 9.x destination property crash
            testTask {
                enabled = false
            }
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
}

dependencies {
    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.682")
    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.682")
    "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    "jsMainImplementation"(npm("chart.js", "4.4.1"))
}
