import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import de.undercouch.gradle.tasks.download.DownloadAction

plugins {
    kotlin("jvm") version "1.5.31"
    id("de.undercouch.download") version "4.1.1"
    application
}

group = "me.michael"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.Lipen.kotlin-satlib:core:0.24.2")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

fun Task.download(action: DownloadAction.() -> Unit) =
    download.configure(delegateClosureOf(action))

val osArch: String = run {
    val osName = System.getProperty("os.name")
    val os = when {
        osName.startsWith("Linux") -> "linux"
        osName.startsWith("Windows") -> "win"
        osName.startsWith("Mac OS X") || osName.startsWith("Darwin") -> "osx"
        else -> return@run "unknown"
    }
    val arch = when (System.getProperty("os.arch")) {
        "x86", "i386" -> "32"
        "x86_64", "amd64" -> "64"
        else -> return@run "unknown"
    }
    "$os$arch"
}

tasks.register("downloadLibs") {
    doLast {
        val urlTemplate = "https://github.com/Lipen/kotlin-satlib/releases/download/0.24.2/%s"
        val libResDir = projectDir.resolve("src/main/resources/lib/$osArch")

        fun ensureDirExists(dir: File) {
            if (!dir.exists()) {
                check(dir.mkdirs()) { "Cannot create dirs for '$dir'" }
            }
            check(dir.exists()) { "'$dir' still does not exist" }
        }

        fun downloadLibs(names: List<String>, dest: File) {
            ensureDirExists(dest)
            download {
                src(names.map { urlTemplate.format(it) })
                dest(dest)
                tempAndMove(true)
            }
        }

        when (osArch) {
            "linux64" -> {
                val jLibs = listOf(
                    "libjminisat.so",
                    "libjglucose.so",
                    "libjcms.so",
                    "libjcadical.so"
                )
                downloadLibs(jLibs, libResDir)

                val solverLibs = listOf(
                    "libminisat.so",
                    "libglucose.so",
                    "libcryptominisat5.so",
                    "libcadical.so"
                )
                val solverLibDir = rootDir.resolve("libs")
                downloadLibs(solverLibs, solverLibDir)
            }
            "win64" -> {
                val jLibs = listOf(
                    "jminisat.dll",
                    "jglucose.dll",
                    "jcadical.dll",
                    "jcms.dll"
                )
                downloadLibs(jLibs, libResDir)

                val solverLibs = listOf(
                    "libminisat.dll",
                    "glucose.dll",
                    "cadical.dll",
                    "libcryptominisat5win.dll"
                )
                val solverLibDir = rootDir.resolve("libs")
                downloadLibs(solverLibs, solverLibDir)
            }
            else -> {
                error("$osArch is not supported, sorry")
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}