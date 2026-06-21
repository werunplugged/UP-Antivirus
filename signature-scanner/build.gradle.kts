plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

android {
    namespace = "com.unplugged.signature_scanner"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        missingDimensionStrategy("environment", "production")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// Inject the private-assets overlay as a *generated* asset source directory wired through the
// AGP variant API, so AGP gives every consumer (merge/lint/package) an explicit dependency on the
// producing task automatically — instead of writing into src/main/assets and hand-wiring only the
// merge*Assets tasks.
abstract class CopyPrivateAssets : DefaultTask() {
    @get:Internal
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val out = outputDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()
        val src = sourceDir.get().asFile
        // Overlay is optional: in CI the real tracker/blacklist data is injected straight into
        // src/main/assets by the builder's S3 step, so a missing private-assets/ dir is not an error.
        if (src.exists()) {
            src.copyRecursively(out, overwrite = true)
        }
    }
}

val copyPrivateAssets = tasks.register<CopyPrivateAssets>("copyPrivateAssets") {
    sourceDir.set(rootProject.layout.projectDirectory.dir("private-assets/signature-scanner/assets"))
    outputDir.set(layout.buildDirectory.dir("generated/privateAssets"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(copyPrivateAssets, CopyPrivateAssets::outputDir)
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("androidx.activity:activity-ktx:1.8.2")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    implementation("com.opencsv:opencsv:4.6")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(":up-antivirus-common"))

}