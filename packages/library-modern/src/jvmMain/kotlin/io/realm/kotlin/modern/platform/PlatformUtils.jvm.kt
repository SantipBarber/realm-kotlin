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
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.Condition
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.StandardOpenOption

public actual object PlatformUtils {
    
    // File System Operations
    public actual fun getDefaultRealmDirectory(): String {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".realm").absolutePath
    }
    
    public actual fun createDirectory(path: String): Boolean {
        return try {
            Files.createDirectories(Paths.get(path))
            true
        } catch (e: IOException) {
            false
        }
    }
    
    public actual fun fileExists(path: String): Boolean = Files.exists(Paths.get(path))
    
    public actual fun deleteFile(path: String): Boolean {
        return try {
            Files.deleteIfExists(Paths.get(path))
        } catch (e: IOException) {
            false
        }
    }
    
    public actual fun getFileSize(path: String): Long {
        return try {
            Files.size(Paths.get(path))
        } catch (e: IOException) {
            0L
        }
    }
    
    public actual fun moveFile(fromPath: String, toPath: String): Boolean {
        return try {
            Files.move(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    public actual fun copyFile(fromPath: String, toPath: String): Boolean {
        return try {
            Files.copy(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // Path Management
    public actual fun joinPath(vararg components: String): String {
        var path = Paths.get(components[0])
        for (i in 1 until components.size) {
            path = path.resolve(components[i])
        }
        return path.toString()
    }
    
    public actual fun getParentDirectory(path: String): String? {
        return Paths.get(path).parent?.toString()
    }
    
    public actual fun getFileName(path: String): String {
        return Paths.get(path).fileName?.toString() ?: ""
    }
    
    public actual fun getFileNameWithoutExtension(path: String): String {
        val filename = getFileName(path)
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(0, lastDotIndex) else filename
    }
    
    public actual fun isAbsolutePath(path: String): Boolean = Paths.get(path).isAbsolute
    
    // Threading and Concurrency
    public actual fun getIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    public actual fun getComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    public actual fun getCurrentThreadName(): String = Thread.currentThread().name
    
    public actual fun isMainThread(): Boolean = Thread.currentThread().name == "main"
    
    // Memory Management
    public actual fun requestGarbageCollection() {
        System.gc()
    }
    
    public actual fun getAvailableMemory(): Long {
        return Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
    }
    
    public actual fun getTotalMemory(): Long = Runtime.getRuntime().maxMemory()
    
    // Platform-specific Error Handling
    public actual fun handleNativeException(exception: Throwable): RealmPlatformException {
        return when (exception) {
            is IOException -> RealmPlatformException("File system error: ${exception.message}", exception)
            is SecurityException -> RealmPlatformException("Security error: ${exception.message}", exception)
            else -> RealmPlatformException("Platform error: ${exception.message}", exception)
        }
    }
    
    public actual fun logPlatformError(message: String, throwable: Throwable?) {
        System.err.println("RealmPlatform [JVM]: $message")
        throwable?.printStackTrace()
    }
}

public actual class RealmPlatformException actual constructor(
    message: String,
    cause: Throwable?
) : Exception(message, cause) {
    
    public actual constructor(message: String) : this(message, null)
    
    public actual val platformErrorCode: Int = when (cause) {
        is IOException -> -1001
        is SecurityException -> -1002
        else -> -1000
    }
    
    public actual val platformErrorMessage: String = cause?.message ?: message
}

public actual object RealmFileSystem {
    
    public actual fun createRealmFile(path: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.createNewFile()
        } catch (e: IOException) {
            false
        }
    }
    
    public actual fun isRealmFileValid(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isFile() && file.canRead()
    }
    
    public actual fun getRealmFileInfo(path: String): RealmFileInfo? {
        val file = File(path)
        return if (file.exists()) {
            RealmFileInfo(
                path = file.absolutePath,
                size = file.length(),
                lastModified = file.lastModified(),
                isReadOnly = !file.canWrite(),
                exists = true
            )
        } else {
            null
        }
    }
    
    public actual fun lockRealmFile(path: String): RealmFileLock? {
        return try {
            val channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE)
            val lock = channel.tryLock()
            if (lock != null) JvmRealmFileLock(channel, lock) else null
        } catch (e: IOException) {
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
        try {
            Files.walk(Paths.get(directory))
                .filter { it.fileName.toString().startsWith("realm_temp_") }
                .forEach { Files.deleteIfExists(it) }
        } catch (e: IOException) {
            PlatformUtils.logPlatformError("Failed to cleanup temp files", e)
        }
    }
    
    public actual fun getRealmTempDirectory(): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        return File(tempDir, "realm_temp").absolutePath
    }
}

private class JvmRealmFileLock(
    private val channel: FileChannel,
    private val lock: FileLock
) : RealmFileLock {
    
    public override fun release(): Boolean {
        return try {
            lock.release()
            channel.close()
            true
        } catch (e: IOException) {
            false
        }
    }
    
    public override fun isValid(): Boolean = lock.isValid
}

public actual object RealmThreading {
    
    private val executor = Executors.newCachedThreadPool { r ->
        Thread(r, "realm-worker-${System.currentTimeMillis()}")
    }
    
    public actual fun createRealmWorkerThread(name: String): RealmWorkerThread {
        return JvmRealmWorkerThread(name)
    }
    
    public actual fun getCurrentThreadId(): Long = Thread.currentThread().id
    
    public actual fun isRealmThread(): Boolean {
        return Thread.currentThread().name.startsWith("realm-")
    }
    
    public actual fun createMutex(): RealmMutex = JvmRealmMutex()
    
    public actual fun createCondition(): RealmCondition = JvmRealmCondition()
    
    public actual fun createReadWriteLock(): RealmReadWriteLock = JvmRealmReadWriteLock()
}

private class JvmRealmWorkerThread(private val name: String) : RealmWorkerThread {
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, name)
    }
    
    public override fun start() {
        // Executor is already started
    }
    
    public override fun stop() {
        executor.shutdown()
    }
    
    public override fun post(action: () -> Unit) {
        executor.execute(action)
    }
    
    public override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        executor.execute {
            Thread.sleep(delayMillis)
            action()
        }
    }
    
    public override val isAlive: Boolean
        get() = !executor.isShutdown
}

private class JvmRealmMutex : RealmMutex {
    private val mutex = java.util.concurrent.locks.ReentrantLock()
    
    public override fun lock() = mutex.lock()
    public override fun unlock() = mutex.unlock()
    public override fun tryLock(): Boolean = mutex.tryLock()
}

private class JvmRealmCondition : RealmCondition {
    private val lock = java.util.concurrent.locks.ReentrantLock()
    private val condition = lock.newCondition()
    
    public override fun wait(mutex: RealmMutex) {
        condition.await()
    }
    
    public override fun wait(mutex: RealmMutex, timeoutMillis: Long): Boolean {
        return condition.await(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
    
    public override fun signal() = condition.signal()
    public override fun signalAll() = condition.signalAll()
}

private class JvmRealmReadWriteLock : RealmReadWriteLock {
    private val rwLock = ReentrantReadWriteLock()
    
    public override fun readLock(): RealmMutex = JvmReadLockWrapper(rwLock.readLock())
    public override fun writeLock(): RealmMutex = JvmWriteLockWrapper(rwLock.writeLock())
}

private class JvmReadLockWrapper(private val readLock: java.util.concurrent.locks.Lock) : RealmMutex {
    public override fun lock() = readLock.lock()
    public override fun unlock() = readLock.unlock()
    public override fun tryLock(): Boolean = readLock.tryLock()
}

private class JvmWriteLockWrapper(private val writeLock: java.util.concurrent.locks.Lock) : RealmMutex {
    public override fun lock() = writeLock.lock()
    public override fun unlock() = writeLock.unlock()
    public override fun tryLock(): Boolean = writeLock.tryLock()
}