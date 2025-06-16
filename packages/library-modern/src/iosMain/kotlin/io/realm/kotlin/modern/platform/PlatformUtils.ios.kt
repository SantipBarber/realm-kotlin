/*
 * Copyright 2024 Realm Inc. & SantipBarber
 * 
 * Modern Interop Layer Implementation
 * Designed and developed by SantipBarber - December 2024
 * 
 * This Modern Interop Layer represents a complete architectural rewrite
 * that eliminates SWIG dependencies and introduces advanced reactive programming.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.kotlin.modern.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*
import platform.darwin.*
import platform.posix.*
import kotlinx.cinterop.*

public actual object PlatformUtils {
    
    // File System Operations
    public actual fun getDefaultRealmDirectory(): String {
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String
        return documentsPath?.let { "$it/realm" } ?: "/tmp/realm"
    }
    
    public actual fun createDirectory(path: String): Boolean {
        return NSFileManager.defaultManager.createDirectoryAtPath(
            path = path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    public actual fun fileExists(path: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }
    
    public actual fun deleteFile(path: String): Boolean {
        return NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    }
    
    public actual fun getFileSize(path: String): Long {
        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
        return (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
    }
    
    public actual fun moveFile(fromPath: String, toPath: String): Boolean {
        return NSFileManager.defaultManager.moveItemAtPath(
            srcPath = fromPath,
            toPath = toPath,
            error = null
        )
    }
    
    public actual fun copyFile(fromPath: String, toPath: String): Boolean {
        return NSFileManager.defaultManager.copyItemAtPath(
            srcPath = fromPath,
            toPath = toPath,
            error = null
        )
    }
    
    // Path Management
    public actual fun joinPath(vararg components: String): String {
        return components.fold("") { acc, component ->
            if (acc.isEmpty()) component else (acc as NSString).stringByAppendingPathComponent(component)
        }
    }
    
    public actual fun getParentDirectory(path: String): String? {
        val parent = (path as NSString).stringByDeletingLastPathComponent
        return if (parent.isEmpty() || parent == path) null else parent
    }
    
    public actual fun getFileName(path: String): String {
        return (path as NSString).lastPathComponent
    }
    
    public actual fun getFileNameWithoutExtension(path: String): String {
        return ((path as NSString).stringByDeletingPathExtension as NSString).lastPathComponent
    }
    
    public actual fun isAbsolutePath(path: String): Boolean {
        return (path as NSString).isAbsolutePath()
    }
    
    // Threading and Concurrency
    public actual fun getIODispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    public actual fun getComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    public actual fun getCurrentThreadName(): String {
        return NSThread.currentThread.name ?: "unnamed-thread"
    }
    
    public actual fun isMainThread(): Boolean = NSThread.isMainThread
    
    // Memory Management
    public actual fun requestGarbageCollection() {
        // iOS uses ARC, no manual GC needed, but we can suggest cleanup
        kotlin.native.runtime.GC.collect()
    }
    
    public actual fun getAvailableMemory(): Long {
        // Simplified implementation - return a reasonable default
        return 512 * 1024 * 1024L // 512MB
    }
    
    public actual fun getTotalMemory(): Long {
        // Simplified implementation - return a reasonable default
        return 2 * 1024 * 1024 * 1024L // 2GB
    }
    
    // Platform-specific Error Handling
    public actual fun handleNativeException(exception: Throwable): RealmPlatformException {
        return RealmPlatformException("Platform error: ${exception.message}", exception)
    }
    
    public actual fun logPlatformError(message: String, throwable: Throwable?) {
        NSLog("RealmPlatform [iOS]: $message")
        throwable?.let { NSLog("Exception: ${it.message}") }
    }
}

public actual class RealmPlatformException actual constructor(
    message: String,
    cause: Throwable?
) : Exception(message, cause) {
    
    public actual constructor(message: String) : this(message, null)
    
    public actual val platformErrorCode: Int = -3000
    
    public actual val platformErrorMessage: String = cause?.message ?: message
}

public actual object RealmFileSystem {
    
    public actual fun createRealmFile(path: String): Boolean {
        val parentDir = PlatformUtils.getParentDirectory(path)
        parentDir?.let { PlatformUtils.createDirectory(it) }
        
        return NSFileManager.defaultManager.createFileAtPath(
            path = path,
            contents = null,
            attributes = null
        )
    }
    
    public actual fun isRealmFileValid(path: String): Boolean {
        val fileManager = NSFileManager.defaultManager
        return fileManager.fileExistsAtPath(path) && fileManager.isReadableFileAtPath(path)
    }
    
    public actual fun getRealmFileInfo(path: String): RealmFileInfo? {
        val fileManager = NSFileManager.defaultManager
        val attributes = fileManager.attributesOfItemAtPath(path, error = null)
        
        return if (attributes != null) {
            RealmFileInfo(
                path = path,
                size = (attributes[NSFileSize] as? NSNumber)?.longLongValue ?: 0L,
                lastModified = ((attributes[NSFileModificationDate] as? NSDate)?.timeIntervalSince1970?.times(1000))?.toLong() ?: 0L,
                isReadOnly = !fileManager.isWritableFileAtPath(path),
                exists = true
            )
        } else {
            null
        }
    }
    
    public actual fun lockRealmFile(path: String): RealmFileLock? {
        // Simplified file locking implementation
        return if (PlatformUtils.fileExists(path)) {
            IOSRealmFileLock(path)
        } else {
            null
        }
    }
    
    public actual fun compactRealmFile(path: String): Boolean {
        // Placeholder implementation - would need actual realm-core integration
        return PlatformUtils.fileExists(path)
    }
    
    public actual fun createBackup(sourcePath: String, backupPath: String): Boolean {
        return PlatformUtils.copyFile(sourcePath, backupPath)
    }
    
    public actual fun restoreFromBackup(backupPath: String, targetPath: String): Boolean {
        return PlatformUtils.copyFile(backupPath, targetPath)
    }
    
    public actual fun cleanupTempFiles(directory: String) {
        val fileManager = NSFileManager.defaultManager
        val contents = fileManager.contentsOfDirectoryAtPath(directory, error = null) as? List<String>
        
        contents?.filter { it.startsWith("realm_temp_") }?.forEach { filename ->
            val fullPath = PlatformUtils.joinPath(directory, filename)
            fileManager.removeItemAtPath(fullPath, error = null)
        }
    }
    
    public actual fun getRealmTempDirectory(): String {
        val tempDir = NSTemporaryDirectory()
        return PlatformUtils.joinPath(tempDir, "realm_temp")
    }
}

private class IOSRealmFileLock(private val path: String) : RealmFileLock {
    private var isReleased = false
    
    public override fun release(): Boolean {
        isReleased = true
        return true
    }
    
    public override fun isValid(): Boolean = !isReleased
}

public actual object RealmThreading {
    
    public actual fun createRealmWorkerThread(name: String): RealmWorkerThread {
        return IOSRealmWorkerThread(name)
    }
    
    public actual fun getCurrentThreadId(): Long {
        return pthread_self().toLong()
    }
    
    public actual fun isRealmThread(): Boolean {
        val threadName = PlatformUtils.getCurrentThreadName()
        return threadName.startsWith("realm-")
    }
    
    public actual fun createMutex(): RealmMutex = IOSRealmMutex()
    
    public actual fun createCondition(): RealmCondition = IOSRealmCondition()
    
    public actual fun createReadWriteLock(): RealmReadWriteLock = IOSRealmReadWriteLock()
}

private class IOSRealmWorkerThread(private val name: String) : RealmWorkerThread {
    private val queue = dispatch_queue_create(name, null)
    private var isRunning = false
    
    public override fun start() {
        isRunning = true
    }
    
    public override fun stop() {
        isRunning = false
    }
    
    public override fun post(action: () -> Unit) {
        dispatch_async(queue) {
            if (isRunning) {
                action()
            }
        }
    }
    
    public override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        val delayNanos = delayMillis * 1_000_000L // Convert millis to nanos
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayNanos), queue) {
            if (isRunning) {
                action()
            }
        }
    }
    
    public override val isAlive: Boolean
        get() = isRunning
}

private class IOSRealmMutex : RealmMutex {
    private val lock = NSLock()
    
    public override fun lock() {
        lock.lock()
    }
    
    public override fun unlock() {
        lock.unlock()
    }
    
    public override fun tryLock(): Boolean {
        return lock.tryLock()
    }
}

private class IOSRealmCondition : RealmCondition {
    private val condition = NSCondition()
    
    public override fun wait(mutex: RealmMutex) {
        condition.wait()
    }
    
    public override fun wait(mutex: RealmMutex, timeoutMillis: Long): Boolean {
        val timeoutSeconds = timeoutMillis.toDouble() / 1000.0
        return condition.waitUntilDate(NSDate.dateWithTimeIntervalSinceNow(timeoutSeconds))
    }
    
    public override fun signal() {
        condition.signal()
    }
    
    public override fun signalAll() {
        condition.broadcast()
    }
}

private class IOSRealmReadWriteLock : RealmReadWriteLock {
    private val readLock = NSLock()
    private val writeLock = NSLock()
    
    public override fun readLock(): RealmMutex = IOSLockWrapper(readLock)
    public override fun writeLock(): RealmMutex = IOSLockWrapper(writeLock)
}

private class IOSLockWrapper(private val lock: NSLock) : RealmMutex {
    public override fun lock() = lock.lock()
    public override fun unlock() = lock.unlock()
    public override fun tryLock(): Boolean = lock.tryLock()
}