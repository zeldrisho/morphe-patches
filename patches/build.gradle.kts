group = "com.zeldrisho.patches"

patches {
    about {
        name = "Zeldris Patches"
        description = "Patches for apps I like, for use with Morphe"
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
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build, "buildAndroid")

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
        environment("PATCHES_BUNDLE", layout.buildDirectory.file("libs/patches-${project.version}.mpp").get().asFile.absolutePath)
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
