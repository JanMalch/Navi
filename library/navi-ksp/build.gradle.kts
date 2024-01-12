plugins {
    kotlin("jvm")
}

group = "io.github.janmalch.navi.ksp"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.16")
    implementation("com.squareup:kotlinpoet-ksp:1.15.3")
    // TODO: reference androix.navigation classes by importing?
    implementation(project(":library:navi-runtime"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
