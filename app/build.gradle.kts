plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.qsee"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qsee"
        minSdk = 27
        targetSdk = 33
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

dependencies {
    //noinspection GradleCompatible
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.android.volley:volley:1.2.1")
    //implementation("com.google.maps.android:polyline:2.1.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-common:20.3.3")
    implementation("com.google.firebase:firebase-database:20.2.2")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-messaging:23.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:android-maps-utils:x.y.z")
        // OLDER
        // implementation("com.google.android.gms:play-services-maps:17.0.0")

    // Places API
    implementation("com.google.android.libraries.places:places:3.2.0")

    // Current Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
        // OLDER
        // implementation("com.google.android.gms:play-services-location:17.0.0")

    //
    implementation ("com.google.maps.android:android-maps-utils:3.5.3")
    implementation ("com.squareup.okhttp3:okhttp:4.+")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        // OLDER VERSION
        // implementation("com.squareup.retrofit2:retrofit:2.4.0")
        // implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")
        // implementation("com.squareup.retrofit2:converter-gson:2.3.0")

    // RX
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.1.1")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
        // OLDER VERSION
        // implementation("io.reactivex.rxjava2:rxjava:2.2.2")
        // implementation("com.jakewharton.rxrelay2:rxrelay:2.0.0")
        // implementation("io.reactivex.rxjava2:rxandroid:2.0.2")

    // ImageView from Firebase
    implementation ("com.squareup.picasso:picasso:2.71828")

    // implementation("androidx.fragment:fragment:1.5.0")

    // implementation("com.google.android.gms:play-services-maps:17.0.0")
    // implementation("com.google.android.gms:play-services-location:18.0.0") // If you need location services
}