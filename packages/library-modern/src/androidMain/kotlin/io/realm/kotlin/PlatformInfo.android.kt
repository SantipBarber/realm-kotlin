package io.realm.kotlin

import android.os.Build

public actual object PlatformInfo {
    public actual fun getPlatformName(): String = "Android"
    public actual fun getPlatformVersion(): String = Build.VERSION.RELEASE
}