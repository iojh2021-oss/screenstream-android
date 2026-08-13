#!/data/data/com.termux/files/usr/bin/bash

set -e

mkdir -p \
app/src/main/java/com/example/screenstream \
app/src/main/res/values \
app/src/main/res/xml \
app/src/main/res/drawable \
app/src/main/res/mipmap-anydpi-v26 \
gradle/wrapper \
.github/workflows


cat > settings.gradle.kts <<'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name="ScreenStream"
include(":app")
EOF


cat > build.gradle.kts <<'EOF'
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}
EOF


cat > gradle.properties <<'EOF'
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
kotlin.code.style=official
EOF


cat > gradle/wrapper/gradle-wrapper.properties <<'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF


cat > .github/workflows/build.yml <<'EOF'
name: Build APK

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: 17

    - uses: android-actions/setup-android@v3

    - uses: gradle/actions/setup-gradle@v4

    - name: Build
      run: ./gradlew assembleDebug

    - uses: actions/upload-artifact@v4
      with:
        name: ScreenStream APK
        path: app/build/outputs/apk/debug/app-debug.apk
EOF


cat > app/build.gradle.kts <<'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace="com.example.screenstream"
    compileSdk=34

    defaultConfig {
        applicationId="com.example.screenstream"
        minSdk=26
        targetSdk=34
        versionCode=1
        versionName="1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled=false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("io.getstream:stream-webrtc-android:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
EOF


cat > app/src/main/AndroidManifest.xml <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"/>

<application
android:theme="@style/Theme.ScreenStream"
android:label="ScreenStream">

<activity
android:name=".MainActivity"
android:exported="true">

<intent-filter>
<action android:name="android.intent.action.MAIN"/>
<category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>

</activity>

<service
android:name=".ScreenCaptureService"
android:foregroundServiceType="mediaProjection"/>

</application>

</manifest>
EOF


cat > app/src/main/res/values/strings.xml <<'EOF'
<resources>
<string name="app_name">ScreenStream</string>
</resources>
EOF


cat > app/src/main/res/values/themes.xml <<'EOF'
<resources>
<style name="Theme.ScreenStream" parent="Theme.AppCompat.Light.NoActionBar"/>
</resources>
EOF


cat > app/src/main/java/com/example/screenstream/MainActivity.kt <<'EOF'
package com.example.screenstream

import android.app.Activity
import android.os.Bundle

class MainActivity: Activity(){

override fun onCreate(savedInstanceState: Bundle?){
super.onCreate(savedInstanceState)
}

}
EOF


cat > app/src/main/java/com/example/screenstream/ScreenCaptureService.kt <<'EOF'
package com.example.screenstream

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ScreenCaptureService: Service(){

override fun onBind(intent:Intent?):IBinder?=null

}
EOF


cat > app/src/main/java/com/example/screenstream/ControlAccessibilityService.kt <<'EOF'
package com.example.screenstream

import android.accessibilityservice.AccessibilityService

class ControlAccessibilityService:
AccessibilityService(){

override fun onAccessibilityEvent(event:android.view.accessibility.AccessibilityEvent?){}

override fun onInterrupt(){}

}
EOF


cat > app/src/main/java/com/example/screenstream/SignalingClient.kt <<'EOF'
package com.example.screenstream

import okhttp3.OkHttpClient

class SignalingClient{

private val client=OkHttpClient()

}
EOF


echo "DONE"
