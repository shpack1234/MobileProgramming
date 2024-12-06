import com.android.sdklib.AndroidVersion.VersionCodes.TIRAMISU

plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "corp.cloudint.fridgeplus"
    compileSdk = 35

    defaultConfig {
        applicationId = "corp.cloudint.fridgeplus"
        minSdk = 31
        targetSdk = TIRAMISU
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

secrets{
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"

    ignoreList.add("sdk.*")
}

dependencies {
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.hilt.android)
    implementation(libs.googleid)
    annotationProcessor(libs.hilt.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.auth)
    implementation(libs.datatype.jackson.datatype.jsr310)
    implementation(libs.credentials)
    implementation(libs.lottie.compose)
    implementation(libs.credentials.play.services.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
