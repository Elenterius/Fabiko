import org.gradle.api.internal.plugins.UnixStartScriptGenerator
import org.gradle.api.internal.plugins.WindowsStartScriptGenerator
import java.io.Writer

plugins {
    id("caliko-conventions")
    application
}

base {
    group = "au.edu.federation.caliko.demo"
    description = "${rootProject.name}-demo"
    archivesName = "${rootProject.name}-demo"
}

application {
    mainClass = "au.edu.federation.caliko.demo.Application"
    applicationName = "Caliko-Demo"
}

enum class LWJGLPlatforms(classifier: String, val isUnix: Boolean) {
    FREEBSD("freebsd", true),
    LINUX("linux", true),
    LINUX_ARM64("linux-arm64", true),
    LINUX_ARM32("linux-arm32", true),
    LINUX_PPC64LE("linux-ppc64le", true),
    LINUX_RISCV64("linux-riscv64", true),
    MACOS("macos", true),
    MACOS_ARM64("macos-arm64", true),
    WINDOWS("windows", false),
    WINDOWS_X86("windows-x86", false),
    WINDOWS_ARM64("windows-arm64", false);

    val classifier: String = "natives-$classifier"

    override fun toString(): String {
        return classifier
    }

    companion object {
        val ALL_UNIX = values().filter { it.isUnix }
        val ALL_WINDOWS = values().filter { !it.isUnix }
    }
}

val envNativeClassifier = Pair(System.getProperty("os.name")!!, System.getProperty("os.arch")!!).let { (name, arch) ->
    when {
        "FreeBSD" == name -> LWJGLPlatforms.FREEBSD.classifier

        arrayOf("Linux", "SunOS", "Unix").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                if (arch.contains("64") || arch.startsWith("armv8")) LWJGLPlatforms.LINUX_ARM64.classifier
                else LWJGLPlatforms.LINUX_ARM32.classifier
            else if (arch.startsWith("ppc"))
                LWJGLPlatforms.LINUX_PPC64LE.classifier
            else if (arch.startsWith("riscv"))
                LWJGLPlatforms.LINUX_RISCV64.classifier
            else
                LWJGLPlatforms.LINUX.classifier

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            if (arch.startsWith("aarch64")) LWJGLPlatforms.MACOS_ARM64.classifier
            else LWJGLPlatforms.MACOS.classifier

        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64")) {
                if (arch.startsWith("aarch64")) LWJGLPlatforms.WINDOWS_ARM64.classifier
                else LWJGLPlatforms.WINDOWS.classifier
            } else
                LWJGLPlatforms.WINDOWS_X86.classifier

        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

val nativeLibrary: Configuration by configurations.creating

dependencies {
    implementation(project(":core"))
    implementation(project(":visualisation"))

    implementation(platform(libs.lwjgl.bom))
    implementation(libs.lwjgl.core)
    implementation(libs.lwjgl.glfw)
    implementation(libs.lwjgl.opengl)
    runtimeOnly(variantOf(libs.lwjgl.core) { classifier(envNativeClassifier) })
    runtimeOnly(variantOf(libs.lwjgl.glfw) { classifier(envNativeClassifier) })
    runtimeOnly(variantOf(libs.lwjgl.opengl) { classifier(envNativeClassifier) })

    nativeLibrary(platform(libs.lwjgl.bom))
    LWJGLPlatforms.values().forEach { platform ->
        nativeLibrary(variantOf(libs.lwjgl.core) { classifier(platform.classifier) })
        nativeLibrary(variantOf(libs.lwjgl.glfw) { classifier(platform.classifier) })
        nativeLibrary(variantOf(libs.lwjgl.opengl) { classifier(platform.classifier) })
    }
}

val nativeLibraries = project.copySpec {
    from(nativeLibrary)
    into("lib")
    include(
        LWJGLPlatforms.values()
            .filterNot { it.classifier == envNativeClassifier }
            .map { platform -> "*${platform.classifier}*" }
    )
}

distributions {
    main {
        contents {
            with(nativeLibraries)
        }
    }
}

tasks.startScripts {
    unixStartScriptGenerator = CustomUnixStartScriptGenerator()
    windowsStartScriptGenerator = CustomWindowsStartScriptGenerator()
}

internal class CustomUnixStartScriptGenerator : UnixStartScriptGenerator() {
    override fun generateScript(details: JavaAppStartScriptGenerationDetails, destination: Writer) {
        super.generateScript(NativeScriptGenerationDetails(details, true), destination)
    }
}

internal class CustomWindowsStartScriptGenerator : WindowsStartScriptGenerator() {
    override fun generateScript(details: JavaAppStartScriptGenerationDetails, destination: Writer) {
        super.generateScript(NativeScriptGenerationDetails(details, false), destination)
    }
}

internal class NativeScriptGenerationDetails(private val details: JavaAppStartScriptGenerationDetails, private val isUnix: Boolean) :
    JavaAppStartScriptGenerationDetails {

    override fun getClasspath(): List<String> {
        val nativeClassifiers = LWJGLPlatforms.values().filter { it.isUnix == isUnix }.map { it.classifier }

        val nativeLibs = nativeLibrary.files
            .filter { file -> nativeClassifiers.any { file.name.contains(it) } }
            .map { "lib/${it.name}" }

        val baseLibs = details.classpath.filterNot { libName -> LWJGLPlatforms.values().any { libName.contains(it.classifier) } }  //strip out all natives

        val distinctClasspath = mutableSetOf<String>()
        distinctClasspath.addAll(baseLibs)
        distinctClasspath.addAll(nativeLibs)

        return distinctClasspath.toList()
    }

    override fun getApplicationName(): String {
        return details.applicationName
    }

    override fun getOptsEnvironmentVar(): String {
        return details.optsEnvironmentVar
    }

    override fun getExitEnvironmentVar(): String {
        return details.exitEnvironmentVar
    }

    override fun getMainClassName(): String {
        return details.mainClassName
    }

    override fun getDefaultJvmOpts(): MutableList<String> {
        return details.defaultJvmOpts
    }

    override fun getModulePath(): MutableList<String> {
        return details.modulePath
    }

    override fun getScriptRelPath(): String {
        return details.scriptRelPath
    }

    override fun getAppNameSystemProperty(): String? {
        return details.appNameSystemProperty
    }

}
