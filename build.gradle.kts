plugins {
    id("org.jetbrains.kotlin.js") version "1.9.22"
}

group = "com.dmrc.roster"
version = "1.0.0"

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "dmrc-roster-pwa.js"
                output.libraryTarget = "umd"
                publicPath = "/dmrc-roster-pwa/"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.682")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.682")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.11.0-pre.682")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.11.0-pre.682")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation(npm("chart.js", "4.4.1"))
                implementation(npm("react-chartjs-2", "5.2.0"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
