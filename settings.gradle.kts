@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
    }
}

// F-Droid doesn't support foojay-resolver plugin
// plugins {
//     id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
// }

rootProject.name = "SonicSpace"
include(
    ":app",
    ":core",
    ":playback",
    ":unison",
    ":innertube",
    ":shazamkit",
    ":artistvideo",
    ":lyrics",
    ":betterlyrics",
    ":kugou",
    ":lrclib",
    ":paxsenixlyrics",
    ":simpmusic",
    ":youlyplus",
    ":canvas",
    ":applecanvas",
    ":echomusiccanvas"
)

// Modular architecture directory configuration
project(":core").projectDir = file("modules/core")
project(":playback").projectDir = file("modules/playback")
project(":unison").projectDir = file("modules/unison")
project(":innertube").projectDir = file("modules/innertube")
project(":shazamkit").projectDir = file("modules/shazamkit")
project(":artistvideo").projectDir = file("modules/artistvideo")
project(":lyrics").projectDir = file("modules/lyrics")
project(":canvas").projectDir = file("modules/canvas")

project(":betterlyrics").projectDir = file("modules/providers/lyrics/betterlyrics")
project(":kugou").projectDir = file("modules/providers/lyrics/kugou")
project(":lrclib").projectDir = file("modules/providers/lyrics/lrclib")
project(":paxsenixlyrics").projectDir = file("modules/providers/lyrics/paxsenixlyrics")
project(":simpmusic").projectDir = file("modules/providers/lyrics/simpmusic")
project(":youlyplus").projectDir = file("modules/providers/lyrics/youlyplus")

project(":applecanvas").projectDir = file("modules/providers/canvas/applecanvas")
project(":echomusiccanvas").projectDir = file("modules/providers/canvas/echomusiccanvas")


// Use a local copy of BravePipe Extractor.
// We assume, that echomusic and BravePipe Extractor have the same parent directory.
// If this is not the case, please change the path in includeBuild().
//
// For this to work you also need to change the implementation in innertube/build.gradle.kts
// to one which does not specify a version.
// From:
//      implementation(libs.newpipe.extractor)
// To:
//      implementation("com.github.bravepipeproject:extractor")
// includeBuild("../BravePipeExtractor") {
//     dependencySubstitution {
//         substitute(module("com.github.bravepipeproject:extractor")).using(project(":extractor"))
//     }
// }
