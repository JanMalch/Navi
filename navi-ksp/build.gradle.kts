plugins {
    kotlin("jvm")
    `maven-publish`
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.16")
    implementation("com.squareup:kotlinpoet-ksp:1.15.3")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register("maven", MavenPublication::class) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}
