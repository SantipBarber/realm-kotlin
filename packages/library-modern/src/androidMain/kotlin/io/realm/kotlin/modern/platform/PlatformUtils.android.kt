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

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.Condition
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.StandardOpenOption

public actual object PlatformUtils {
    
    internal var applicationContext: Context? = null
    
    /**
     * Initialize with Android application context
     */
    public fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    
    // File System Operations
    public actual fun getDefaultRealmDirectory(): String {
        val context = requireApplicationContext()
        return File(context.filesDir, "realm").absolutePath
    }
    
    public actual fun createDirectory(path: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.createDirectories(Paths.get(path))
                true
            } catch (e: IOException) {
                false
            }
        } else {
            File(path).mkdirs()
        }
    }
    
    public actual fun fileExists(path: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.exists(Paths.get(path))
        } else {
            File(path).exists()
        }
    }
    
    public actual fun deleteFile(path: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.deleteIfExists(Paths.get(path))
            } catch (e: IOException) {
                false
            }
        } else {
            File(path).delete()
        }
    }
    
    public actual fun getFileSize(path: String): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.size(Paths.get(path))
            } catch (e: IOException) {
                0L
            }
        } else {
            File(path).length()
        }
    }
    
    public actual fun moveFile(fromPath: String, toPath: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.move(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING)
                true
            } catch (e: IOException) {
                false
            }
        } else {
            File(fromPath).renameTo(File(toPath))
        }
    }
    
    public actual fun copyFile(fromPath: String, toPath: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.copy(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING)
                true
            } catch (e: IOException) {
                false
            }
        } else {
            try {
                File(fromPath).copyTo(File(toPath), overwrite = true)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Path Management
    public actual fun joinPath(vararg components: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var path = Paths.get(components[0])
            for (i in 1 until components.size) {
                path = path.resolve(components[i])
            }
            path.toString()
        } else {
            File(components[0]).let { file ->
                components.drop(1).fold(file) { acc, component ->
                    File(acc, component)
                }.absolutePath
            }
        }
    }
    
    public actual fun getParentDirectory(path: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Paths.get(path).parent?.toString()
        } else {
            File(path).parent
        }
    }
    
    public actual fun getFileName(path: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Paths.get(path).fileName?.toString() ?: ""
        } else {
            File(path).name
        }
    }
    
    public actual fun getFileNameWithoutExtension(path: String): String {
        val filename = getFileName(path)
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(0, lastDotIndex) else filename
    }
    
    public actual fun isAbsolutePath(path: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Paths.get(path).isAbsolute
        } else {
            File(path).isAbsolute
        }
    }
    
    // Threading and Concurrency
    public actual fun getIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    public actual fun getComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    public actual fun getCurrentThreadName(): String = Thread.currentThread().name
    
    public actual fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
    
    // Memory Management
    public actual fun requestGarbageCollection() {
        System.gc()
    }
    
    public actual fun getAvailableMemory(): Long {
        val context = applicationContext
        return if (context != null) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.availMem
        } else {
            Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
        }
    }
    
    public actual fun getTotalMemory(): Long {
        val context = applicationContext
        return if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.totalMem
        } else {
            Runtime.getRuntime().maxMemory()
        }
    }
    
    // Platform-specific Error Handling
    public actual fun handleNativeException(exception: Throwable): RealmPlatformException {
        return when (exception) {
            is IOException -> RealmPlatformException("File system error: ${exception.message}", exception)
            is SecurityException -> RealmPlatformException("Security error: ${exception.message}", exception)
            else -> RealmPlatformException("Platform error: ${exception.message}", exception)
        }
    }
    
    public actual fun logPlatformError(message: String, throwable: Throwable?) {
        android.util.Log.e("RealmPlatform", message, throwable)
    }
    
    private fun requireApplicationContext(): Context {
        return applicationContext ?: throw IllegalStateException(
            "PlatformUtils not initialized. Call PlatformUtils.initialize(context) from your Application.onCreate()"
        )
    }
}

public actual class RealmPlatformException actual constructor(
    message: String,
    cause: Throwable?
) : Exception(message, cause) {
    
    public actual constructor(message: String) : this(message, null)
    
    public actual val platformErrorCode: Int = when (cause) {
        is IOException -> -2001
        is SecurityException -> -2002
        else -> -2000
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE)
                val lock = channel.tryLock()
                if (lock != null) AndroidRealmFileLock(channel, lock) else null
            } catch (e: IOException) {
                null
            }
        } else {
            // Fallback for older Android versions
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
            val dir = File(directory)
            dir.listFiles()?.filter { it.name.startsWith("realm_temp_") }?.forEach { it.delete() }
        } catch (e: Exception) {
            PlatformUtils.logPlatformError("Failed to cleanup temp files", e)
        }
    }
    
    public actual fun getRealmTempDirectory(): String {
        val context = PlatformUtils.applicationContext
        return if (context != null) {
            File(context.cacheDir, "realm_temp").absolutePath
        } else {
            File(System.getProperty("java.io.tmpdir"), "realm_temp").absolutePath
        }
    }
}

private class AndroidRealmFileLock(
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
    
    public actual fun createRealmWorkerThread(name: String): RealmWorkerThread {
        return AndroidRealmWorkerThread(name)
    }
    
    public actual fun getCurrentThreadId(): Long = Thread.currentThread().id
    
    public actual fun isRealmThread(): Boolean {
        return Thread.currentThread().name.startsWith("realm-")
    }
    
    public actual fun createMutex(): RealmMutex = AndroidRealmMutex()
    
    public actual fun createCondition(): RealmCondition = AndroidRealmCondition()
    
    public actual fun createReadWriteLock(): RealmReadWriteLock = AndroidRealmReadWriteLock()
}

private class AndroidRealmWorkerThread(private val name: String) : RealmWorkerThread {
    private val handlerThread = HandlerThread(name)
    private lateinit var handler: Handler
    
    public override fun start() {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }
    
    public override fun stop() {
        handlerThread.quitSafely()
    }
    
    public override fun post(action: () -> Unit) {
        handler.post(action)
    }
    
    public override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMillis)
    }
    
    public override val isAlive: Boolean
        get() = handlerThread.isAlive
}

private class AndroidRealmMutex : RealmMutex {
    private val mutex = ReentrantLock()
    
    public override fun lock() = mutex.lock()
    public override fun unlock() = mutex.unlock()
    public override fun tryLock(): Boolean = mutex.tryLock()
}

private class AndroidRealmCondition : RealmCondition {
    private val lock = ReentrantLock()
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

private class AndroidRealmReadWriteLock : RealmReadWriteLock {
    private val rwLock = ReentrantReadWriteLock()
    
    public override fun readLock(): RealmMutex = AndroidReadLockWrapper(rwLock.readLock())
    public override fun writeLock(): RealmMutex = AndroidWriteLockWrapper(rwLock.writeLock())
}

private class AndroidReadLockWrapper(private val readLock: java.util.concurrent.locks.Lock) : RealmMutex {
    public override fun lock() = readLock.lock()
    public override fun unlock() = readLock.unlock()
    public override fun tryLock(): Boolean = readLock.tryLock()
}

private class AndroidWriteLockWrapper(private val writeLock: java.util.concurrent.locks.Lock) : RealmMutex {
    public override fun lock() = writeLock.lock()
    public override fun unlock() = writeLock.unlock()
    public override fun tryLock(): Boolean = writeLock.tryLock()
}