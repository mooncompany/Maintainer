buildscript {
    extra.apply {
        set("kotlin_version", "1.9.22")
        set("compose_version", "1.5.8")
    }
}

plugins {
    id("com.android.application") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}