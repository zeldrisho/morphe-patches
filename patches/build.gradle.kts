import java.util.zip.ZipFile

// Single source of truth for the companion-extension wiring on the Gradle
// side. The Kotlin call site (HideAdsPatch.kt extendWith(...)) cannot import
// build-script values across that boundary, so it points back here in a
// comment and any rename must change both together.
val extensionProjects = listOf(":extensions:threads", ":extensions:zalo")
val extensionArtifacts = mapOf(
    ":extensions:threads" to ("morphe/extensions/extension.mpe" to "extensions/extension.mpe"),
    ":extensions:zalo" to ("morphe/extensions/zalo.mpe" to "extensions/zalo.mpe"),
)
plugins {
    // Matches the Kotlin 2.4.10 compiler supplied by Morphe; see docs/development.md.
    id("dev.detekt") version "2.0.0-alpha.6"
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

group = "com.zeldrisho.patches"

patches {
    about {
        name = "Zeldris Patches"
        description = "Patches for Threads and apps I like, for use with Morphe"
        source = "git@github.com:zeldrisho/morphe-patches.git"
        author = "Zeldris"
        contact = "https://github.com/zeldrisho"
        website = "https://github.com/zeldrisho/morphe-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
    testImplementation(libs.gson)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.smali)
}

tasks {
    test {
        dependsOn("generatePatchesListForVerification")
        systemProperty("patches.list.path", layout.buildDirectory.file("verification/patches-list.json").get().asFile.absolutePath)
        // Make opt-in local APK validation cache-correct; CI uses synthetic fixtures.
        val apkInputs = listOf("THREADS_TEST_APK", "ZALO_TEST_APK")
        apkInputs.forEach { variable ->
            val apkPath = providers.environmentVariable(variable).orNull.orEmpty()
            inputs.property(variable, apkPath)
            if (apkPath.isNotBlank()) {
                inputs.file(rootProject.file(apkPath)).withPropertyName("${variable}File")
                environment(variable, rootProject.file(apkPath).absolutePath)
            }
        }
    }

    // Fail-fast pre-check only: confirms the extension module produced its
    // plugin-owned artifact before the full bundle build runs. This shortens
    // the failure loop; the authoritative signal stays the embedded-in-.mpp
    // check in verifyBundleExtension below.
    register("checkExtensionArtifact") {
        description = "Fail fast when the extension module output is missing before building"
        dependsOn(extensionProjects.map { "$it:syncExtension" })
        doLast {
            extensionArtifacts.forEach { (projectPath, paths) ->
                val mpe = project(projectPath).layout.buildDirectory.file(paths.first).get().asFile
                check(mpe.isFile) {
                    "Missing extension artifact $mpe after $projectPath:syncExtension — " +
                        "check $projectPath:mergeDexRelease output."
                }
            }
        }
    }

    register<JavaExec>("verifyEmbeddedDexContracts") {
        description = "Verify exact public static descriptors in embedded extension DEX files"
        dependsOn("buildAndroid", "testClasses")
        classpath = sourceSets["test"].runtimeClasspath
        mainClass.set("com.zeldrisho.patches.bundle.EmbeddedDexContractVerifierKt")
        args(layout.buildDirectory.file("libs/patches-${project.version}.mpp").get().asFile.absolutePath)
    }

    // The Morphe Gradle plugin publishes the extension modules' build/morphe
    // directories and consumes them as patches resources, so the built .mpp
    // already embeds both extension artifacts. extendWith loads
    // it via the bundle classloader (ClassLoader.getResourceAsStream), not
    // from a repo-relative filesystem path. This guard fails fast after the
    // build when the embedded dex is missing instead of shipping a bundle
    // whose Hide-ads patch silently no-ops at runtime.
    register("verifyBundleExtension") {
        description = "Fail fast when the built .mpp misses the embedded extension dex"
        dependsOn("buildAndroid", "testClasses", "verifyEmbeddedDexContracts")
        doLast {
            val libs = layout.buildDirectory.dir("libs").get().asFile
            val mpps = libs.listFiles { file ->
                file.name.endsWith(".mpp") &&
                    !file.name.contains("sources") &&
                    !file.name.contains("javadoc")
            }?.toList().orEmpty()
            val currentMpps = mpps.filter { it.name == "patches-${project.version}.mpp" }
            check(currentMpps.size == 1) {
                "Expected one distributable patches-${project.version}.mpp in $libs, found: ${mpps.map { it.name }}"
            }
            val mpp = currentMpps.single()
            ZipFile(mpp).use { zip ->
                extensionArtifacts.values.forEach { paths ->
                    val entry = zip.getEntry(paths.second)
                    check(entry != null) {
                        "Bundle ${mpp.name} is missing ${paths.second} — " +
                            "check the corresponding extension sync task output."
                    }
                    check(entry.size > 0) { "Embedded extension ${paths.second} is empty" }
                }
                check(
                    zip.entries().asSequence()
                        .filter { it.name.endsWith(".mpe") }
                        .map { it.name }
                        .toSet() == extensionArtifacts.values.map { it.second }.toSet(),
                ) {
                    "Bundle contains unexpected or cross-app extension artifacts"
                }
            }
        }
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
        environment("PATCHES_BUNDLE", layout.buildDirectory.file("libs/patches-${project.version}.mpp").get().asFile.absolutePath)
    }

    register("qualifyZaloApk") {
        group = "verification"
        description = "Run pinned Zalo APK qualification; requires ZALO_TEST_APK"
        dependsOn(test)
        doLast {
            val apk = providers.environmentVariable("ZALO_TEST_APK").orNull
                ?.let(::file)
                ?: error("ZALO_TEST_APK is required for qualifyZaloApk")
            check(apk.isFile && apk.length() > 0) {
                "ZALO_TEST_APK does not name a non-empty APK: $apk"
            }

            fun badging(input: java.io.File): String {
                val process = ProcessBuilder("aapt", "dump", "badging", input.absolutePath)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().use { it.readText() }
                check(process.waitFor() == 0) { "aapt could not read APK metadata: $input" }
                return output
            }

            fun signingCertificate(input: java.io.File): String {
                val process = ProcessBuilder("apksigner", "verify", "--print-certs", input.absolutePath)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().use { it.readText() }
                check(process.waitFor() == 0) { "apksigner could not inspect APK: $input" }
                return output.lineSequence()
                    .filter { it.trimStart().startsWith("Signer #") || it.contains("certificate SHA-256") }
                    .joinToString("\n")
            }

            val metadata = badging(apk)
            val checks = linkedMapOf<String, Boolean>(
                "package" to Regex("package: name='com\\.zing\\.zalo'").containsMatchIn(metadata),
                "version code" to Regex("versionCode='260801903'").containsMatchIn(metadata),
            )
            // APKMirror distributes the native payload in the arm64 config split.
            // Accept a base.apk together with its adjacent split, while still
            // requiring the caller to provide the exact pinned native library.
            val inputs = listOf(apk, apk.resolveSibling("split_config.arm64_v8a.apk"))
                .filter { it.isFile }
            val baseCertificate = signingCertificate(apk)
            inputs.drop(1).forEach { split ->
                val splitMetadata = badging(split)
                check(Regex("package: name='com\\.zing\\.zalo'").containsMatchIn(splitMetadata)) {
                    "APK split has an unexpected package: $split"
                }
                check(Regex("versionCode='260801903'").containsMatchIn(splitMetadata)) {
                    "APK split has an unexpected version: $split"
                }
                check(signingCertificate(split) == baseCertificate) {
                    "APK split signing certificate does not match base APK: $split"
                }
            }
            var hasNativeLibrary = false
            var hasArm64Native = false
            var unsupportedAbi = false
            inputs.forEach { input ->
                ZipFile(input).use { zip ->
                    if (zip.getEntry("lib/arm64-v8a/libnative_utils.so") != null) {
                        hasNativeLibrary = true
                        hasArm64Native = true
                    }
                    zip.entries().asSequence()
                        .filter { it.name.startsWith("lib/") && it.name.endsWith(".so") }
                        .map { it.name.substringAfter("lib/").substringBefore('/') }
                        .forEach { if (it != "arm64-v8a") unsupportedAbi = true }
                }
            }
            checks["arm64 ABI"] = hasArm64Native || Regex("native-code='[^']*arm64-v8a").containsMatchIn(metadata)
            checks["native library"] = hasNativeLibrary
            checks["unsupported ABI absent"] = !unsupportedAbi
            checks.forEach { (name, passed) -> logger.lifecycle("Zalo APK $name: ${if (passed) "PASS" else "FAIL"}") }
            check(checks.values.all { it }) {
                "Pinned Zalo APK qualification failed; expected versionCode 260801903, " +
                    "arm64-v8a, and lib/arm64-v8a/libnative_utils.so"
            }
        }
    }

    register<JavaExec>("generatePatchesListForVerification") {
        description = "Generate current-source patch metadata in an isolated build directory"
        dependsOn("classes")
        val output = layout.buildDirectory.file("verification/patches-list.json")
        outputs.file(output)
        doFirst { output.get().asFile.parentFile.mkdirs() }
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
        environment("PATCHES_LIST_OUTPUT", output.get().asFile.absolutePath)
        environment("PATCHES_BUNDLE", layout.buildDirectory.file("libs/patches-${project.version}.mpp").get().asFile.absolutePath)
    }
}

// Run the lightweight extension pre-check before the full bundle build so a
// missing/broken extension fails in seconds; verifyBundleExtension remains
// the authoritative pass/fail signal on the embedded artifact.
tasks.matching { it.name == "buildAndroid" }.configureEach {
    dependsOn("checkExtensionArtifact")
}
