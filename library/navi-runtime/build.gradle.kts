plugins {
    kotlin("jvm")
}

group = "io.github.janmalch.navi.runtime"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
