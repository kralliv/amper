import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.tooling.GradleConnector
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

fun run(buildRequest: AndroidBuildRequest, debug: Boolean = false): AndroidBuildResult {
    // todo: temp directory isn't the best place for such temporary project, because we don't utilize gradle caches,
    //  but ok for debug
    val tempDir = createTempDirectory()
    val settingsGradle = tempDir.resolve("settings.gradle.kts")
    val settingsGradleFile = settingsGradle.toFile()
    settingsGradleFile.createNewFile()
    // todo: hide by feature flag building plugin from source
    settingsGradleFile.writeText(
        """
pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        includeBuild("${Path.of("../../../").toAbsolutePath().normalize()}")
    }
}


plugins {
    id("org.jetbrains.amper.android.settings.plugin")
}

configure<AmperAndroidIntegrationExtension> {
    jsonData = ${"\"\"\""}${Json.encodeToString(buildRequest)}${"\"\"\""}
}
""".trimIndent()
    )
    val connection = GradleConnector
        .newConnector()
        .forProjectDirectory(settingsGradleFile.parentFile)
        .connect()

    val buildLauncher = connection
        .action { controller -> controller.getModel(AndroidBuildResult::class.java) }
        .forTasks(
            "assemble${
                when (buildRequest.buildType) {
                    AndroidBuildRequest.BuildType.Debug -> "Debug"
                    AndroidBuildRequest.BuildType.Release -> "Release"
                }
            }"
        )
        .withArguments("--stacktrace")
        .setStandardOutput(System.out)
        .setStandardError(System.err)

    if (debug) {
        buildLauncher.addJvmArguments("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    }
    return buildLauncher.run()
}