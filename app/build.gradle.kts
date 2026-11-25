plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.note.dynamic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.note.dynamic"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 仅保留中文资源，剔除依赖库携带的其他语言，显著减小体积
        resourceConfigurations += listOf("zh", "en")
    }

    buildTypes {
        release {
            // 开启代码压缩与资源缩减，是减小 APK 体积最有效的手段
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module",
                "DebugProbesKt.bin"
            )
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    // 项目未使用 @Preview，移除 ui-tooling-preview 以减小体积
    implementation("androidx.compose.material3:material3")
    // 图标库：代码使用了 Outlined/Rounded 扩展图标，保留；R8 会剔除未引用的图标
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // 原 datastore-preferences 依赖在代码中从未使用，已移除以减小体积

    implementation("io.coil-kt:coil-compose:2.7.0")
}
