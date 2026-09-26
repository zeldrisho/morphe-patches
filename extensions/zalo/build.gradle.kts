extension {
    name = "extensions/zalo.mpe"
}

android {
    namespace = "com.zeldrisho.zalo.extension"
    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
