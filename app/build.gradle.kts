import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

fun getVersionCode(): Int = try {
    val code = providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim()
    println("Version code: $code")
    code.toIntOrNull() ?: 0
} catch (_: Exception) {
    0
}

android {
    namespace = "com.amazon.ivs.realtimecollab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amazon.ivs.realtimecollab"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.${getVersionCode()}"

        /**
         * Add your AUTH_URL String in local.properties file.
         * F.e.: AUTH_URL=https://<your-api>.com/
         */
        val localProps = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) load(file.inputStream())
        }
        val authUrl = localProps.getProperty("AUTH_URL") ?: throw GradleException("Missing AUTH_URL in local.properties")
        buildConfigField(type = "String", name = "AUTH_URL", value = "\"$authUrl\"")
    }

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            outputFileName = "IVS-Real-Time-v$versionName.apk"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    configurations.all {
        resolutionStrategy {
            force(libs.navigation.common)
            force(libs.navigation.runtime)
            force(libs.navigation.fragment)
        }
    }
}

dependencies {
    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.window)
    implementation(libs.core.composables)

    // Retrofit
    implementation(libs.okhttp)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit.converter)

    // Amplify
    implementation(libs.aws.api)
    implementation(libs.aws.auth.cognito)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)

    // Timber
    implementation(libs.timber)

    // Chat
    implementation(libs.ivs.chat.messaging)

    // Stages SDK
    implementation(libs.ivs.broadcast) {
        artifact {
            classifier = "stages"
            type = "aar"
        }
    }
}
