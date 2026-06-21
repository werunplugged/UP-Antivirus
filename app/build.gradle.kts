import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

// Load signing properties from user.properties
val keystoreProperties = Properties().apply {
    val primaryPath = "${System.getProperty("user.home")}/Documents/up_dev_signature/signing.properties"
    val fallbackPath = "signing.properties"

    val propsFile = File(primaryPath).takeIf { it.exists() }
        ?: rootProject.file(fallbackPath).takeIf { it.exists() }
        ?: error("No signing.properties found at $primaryPath or $fallbackPath")

    propsFile.reader().use { load(it) }
}
android {
    namespace = "com.unplugged.antivirus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unplugged.antivirus"
        minSdk = 24
        targetSdk = 34
        versionCode = 133
        versionName = "2.31.31"

        manifestPlaceholders["min_version_app_center"] = 257

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        applicationVariants.all {
            val versionName = versionName
            val buildType = buildType.name.lowercase()
            val flavorName = flavorName
            val appNamePrefix = "up_antivirus"

            this.outputs.all {
                val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                output.outputFileName = "${appNamePrefix}_${versionName}_${flavorName}_${buildType}.apk"
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
        }
        create("production") {
            dimension = "environment"
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = file(keystoreProperties.getProperty("keystorePath"))
            storePassword = keystoreProperties.getProperty("keystorePassword")
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            multiDexEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }
}

// Inject the private-assets overlay as a *generated* asset source directory wired through the
// AGP variant API. AGP then gives every consumer (merge/lint/package) an explicit dependency on
// the producing task automatically — instead of writing into src/main/assets and hand-wiring only
// the merge*Assets tasks (which left lint-vital with an unwired implicit dependency).
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
    sourceDir.set(rootProject.layout.projectDirectory.dir("private-assets/app/assets"))
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
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.transition:transition:1.4.1")

    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation(project(":hypatia-extensions"))
    implementation(project(":tracker-extension"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // https://developer.android.com/jetpack/androidx/releases/
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    annotationProcessor("androidx.annotation:annotation:1.7.1")

    // Tracker detection
    implementation("com.github.lanchon.dexpatcher:multidexlib2:2.3.4.r2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.tapadoo:alerter:7.2.4")

    //work manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(project(":signature-scanner"))
    implementation(project(":up-antivirus-common"))

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("com.squareup.inject:assisted-inject-annotations-dagger2:0.6.0")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.6.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation(project(":up-resources"))

}