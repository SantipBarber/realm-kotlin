package io.realm.kotlin

public actual object PlatformInfo {
    public actual fun getPlatformName(): String = "iOS"
    public actual fun getPlatformVersion(): String = "iOS 13+"
}