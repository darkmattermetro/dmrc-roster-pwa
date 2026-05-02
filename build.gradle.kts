plugins {
    id("org.jetbrains.kotlin.js") version "1.9.22"
}

group = "com.dmrc.roster"
version = "1.0.0"

kotlin {
    js(IR) {
        browser {
            // Disable tests to avoid the 'destination' property crash on GitHub Actions
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

    sourceSets {
        val main by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.682")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.682")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation(npm("chart.js", "4.4.1"))
            }
        }
    }
}
