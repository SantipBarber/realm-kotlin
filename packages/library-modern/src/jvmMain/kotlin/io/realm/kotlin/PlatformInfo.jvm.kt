package io.realm.kotlin

public actual object PlatformInfo {
    public actual fun getPlatformName(): String = "JVM"
    public actual fun getPlatformVersion(): String = System.getProperty("java.version") ?: "Unknown"
}