plugins {
    id("fabiko-conventions")
    `java-library`
}

base {
    description = "${rootProject.name}-visualisation"
    archivesName = "${rootProject.name}-visualisation"
}

dependencies {
    api(project(":core"))

    api(platform(libs.lwjgl.bom))
    api(libs.lwjgl.core)
    api(libs.lwjgl.glfw)
    api(libs.lwjgl.opengl)
}
