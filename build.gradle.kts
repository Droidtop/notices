// A plain Kotlin/JVM build so this library is tested on its own; products compile the sources into their own
// modules (droidtop: runtime-common's srcDir), pinned as a git submodule.
plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}
