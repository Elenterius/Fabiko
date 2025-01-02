// this module contains the core IK portion of the library

plugins {
    id("caliko-conventions")
    `java-library`
    alias(libs.plugins.jmh)
    alias(libs.plugins.jmhreport)
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
    //we pick json for displaying the results with JMH Visualizer
    resultFormat = "json" //text, csv, scsv, json, latex
    //resultsFile = project.layout.buildDirectory.file("results/jmh/${time}-results.json")
    humanOutputFile = project.layout.buildDirectory.file("reports/jmh/text/${System.currentTimeMillis()}-human.txt")
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}

jmhReport {
    project.layout.buildDirectory.file("reports/jmh/visualizer").get().asFile.mkdirs()

    jmhResultPath = project.layout.buildDirectory.file("results/jmh/results.json").get().asFile.path
    jmhReportOutput = project.layout.buildDirectory.file("reports/jmh/visualizer").get().asFile.path
}
