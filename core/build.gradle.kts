// this module contains the core IK portion of the library

plugins {
    id("caliko-conventions")
    `java-library`
}

base {
    description = "${rootProject.name}-core"
    archivesName = "${rootProject.name}-core"
}

dependencies {
    api(libs.jspecify.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
