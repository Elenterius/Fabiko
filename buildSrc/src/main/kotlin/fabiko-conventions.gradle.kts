
plugins {
    java
    idea
    `maven-publish`
}

base {
    group = "com.github.elenterius.fabiko"
    version = "0.0.1"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.description,
            "Implementation-Version" to project.version
        )
    }

    doLast {
        project.copy {
            from(archiveFile)
            into(rootProject.layout.buildDirectory.dir("libs"))
        }
    }
}