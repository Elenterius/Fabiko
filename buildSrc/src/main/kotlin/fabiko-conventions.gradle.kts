
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
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.description,
            "Implementation-Version" to project.version
        )
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
