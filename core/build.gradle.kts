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
    api(libs.joml)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform)

    testImplementation(libs.gson)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

val buildDirectory = project.layout.buildDirectory
val timestamp = System.currentTimeMillis()

jmh {
//    profilers.add("stack")
    includes.add("com.github.elenterius.caliko.FabrikChain3DSolverBenchmarks.*")
//    includes.add("com.github.elenterius.caliko.CloneIKChainBenchmarks.*")

    //we pick json for displaying the results with JMH Visualizer
    resultFormat = "json" //text, csv, scsv, json, latex
    humanOutputFile = buildDirectory.file("reports/jmh/text/${timestamp}-human.txt")
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}

jmhReport {
    jmhResultPath = buildDirectory.file("results/jmh/results.json").get().asFile.path
    jmhReportOutput = buildDirectory.file("reports/jmh/visualizer").get().asFile.path
}

tasks.jmhReport {
    group = "jmh"
    doFirst {
        buildDirectory.file("reports/jmh/visualizer").get().asFile.mkdirs()
    }
}