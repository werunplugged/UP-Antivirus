import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}

fun resolveBaseUrl(flavorName: String): String {
    return localProps.getProperty("unplugged.base.url.$flavorName")
        ?: localProps.getProperty("unplugged.base.url")
        ?: error("Missing 'unplugged.base.url.$flavorName' in local.properties")
}

android {
    namespace = "com.unplugged.upantiviruscommon"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

    }

    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"${resolveBaseUrl("development")}\"")
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"${resolveBaseUrl("staging")}\"")
        }
        create("production") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"${resolveBaseUrl("production")}\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
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

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation(project(":tracker-extension"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.picasso:picasso:2.71828")

    "developmentApi"(project(mapOf("path" to ":account-helper", "configuration" to "developmentDefault")))
    "stagingApi"(project(mapOf("path" to ":account-helper", "configuration" to "stagingDefault")))
    "productionApi"(project(mapOf("path" to ":account-helper", "configuration" to "productionDefault")))

    api(project(mapOf("path" to ":attestation-helper", "configuration" to "default")))

    implementation ("dnsjava:dnsjava:3.5.0")

    implementation("com.airbnb.android:lottie:6.6.7")
    implementation("com.goterl:lazysodium-android:5.1.0@aar")
    implementation("net.java.dev.jna:jna:5.8.0@aar")
}