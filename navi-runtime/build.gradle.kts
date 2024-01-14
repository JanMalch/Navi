plugins {
    kotlin("jvm")
    `maven-publish`
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

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
