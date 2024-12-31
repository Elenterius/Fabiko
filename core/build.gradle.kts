// this module contains the core IK portion of the library

plugins {
    id("caliko-conventions")
    `java-library`
    alias(libs.plugins.jmh)
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

jmh {
//    humanOutputFile = project.layout.buildDirectory.file("reports/jmh/human.txt")
//    resultsFile = project.layout.buildDirectory.file("reports/jmh/results.json")

    //we pick json for displaying the results in the JMH Visualizer
    resultFormat = "json" //text, csv, scsv, json, latex
}
