plugins {
    kotlin("js") version "1.9.20"
}

group = "com.dmrc.roster"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
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
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.682")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.682")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation(npm("chart.js", "4.4.1"))
}
