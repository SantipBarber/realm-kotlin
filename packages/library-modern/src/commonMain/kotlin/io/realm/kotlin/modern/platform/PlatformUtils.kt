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

/**
 * Cross-platform utilities for file system operations, threading, and platform-specific functionality
 * needed by Realm Modern implementation.
 */
public expect object PlatformUtils {
    
    // File System Operations
    public fun getDefaultRealmDirectory(): String
    public fun createDirectory(path: String): Boolean
    public fun fileExists(path: String): Boolean
    public fun deleteFile(path: String): Boolean
    public fun getFileSize(path: String): Long
    public fun moveFile(fromPath: String, toPath: String): Boolean
    public fun copyFile(fromPath: String, toPath: String): Boolean
    
    // Path Management
    public fun joinPath(vararg components: String): String
    public fun getParentDirectory(path: String): String?
    public fun getFileName(path: String): String
    public fun getFileNameWithoutExtension(path: String): String
    public fun isAbsolutePath(path: String): Boolean
    
    // Threading and Concurrency
    public fun getIODispatcher(): CoroutineDispatcher
    public fun getComputationDispatcher(): CoroutineDispatcher
    public fun getCurrentThreadName(): String
    public fun isMainThread(): Boolean
    
    // Memory Management
    public fun requestGarbageCollection()
    public fun getAvailableMemory(): Long
    public fun getTotalMemory(): Long
    
    // Platform-specific Error Handling
    public fun handleNativeException(exception: Throwable): RealmPlatformException
    public fun logPlatformError(message: String, throwable: Throwable?)
}

/**
 * Platform-specific exception wrapper
 */
public expect class RealmPlatformException : Exception {
    public constructor(message: String)
    public constructor(message: String, cause: Throwable?)
    
    public val platformErrorCode: Int
    public val platformErrorMessage: String
}

/**
 * File system utilities specific to Realm database operations
 */
public expect object RealmFileSystem {
    
    // Database File Operations
    public fun createRealmFile(path: String): Boolean
    public fun isRealmFileValid(path: String): Boolean
    public fun getRealmFileInfo(path: String): RealmFileInfo?
    public fun lockRealmFile(path: String): RealmFileLock?
    public fun compactRealmFile(path: String): Boolean
    
    // Backup and Recovery
    public fun createBackup(sourcePath: String, backupPath: String): Boolean
    public fun restoreFromBackup(backupPath: String, targetPath: String): Boolean
    
    // Cleanup Operations
    public fun cleanupTempFiles(directory: String)
    public fun getRealmTempDirectory(): String
}

/**
 * Information about a Realm database file
 */
public data class RealmFileInfo(
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isReadOnly: Boolean,
    val exists: Boolean
)

/**
 * Platform-specific file locking mechanism
 */
public interface RealmFileLock {
    public fun release(): Boolean
    public fun isValid(): Boolean
}

/**
 * Threading utilities specific to Realm operations
 */
public expect object RealmThreading {
    
    // Thread Pool Management
    public fun createRealmWorkerThread(name: String): RealmWorkerThread
    public fun getCurrentThreadId(): Long
    public fun isRealmThread(): Boolean
    
    // Synchronization Primitives
    public fun createMutex(): RealmMutex
    public fun createCondition(): RealmCondition
    public fun createReadWriteLock(): RealmReadWriteLock
}

/**
 * Realm-specific worker thread
 */
public interface RealmWorkerThread {
    public fun start()
    public fun stop()
    public fun post(action: () -> Unit)
    public fun postDelayed(delayMillis: Long, action: () -> Unit)
    public val isAlive: Boolean
}

/**
 * Cross-platform mutex implementation
 */
public interface RealmMutex {
    public fun lock()
    public fun unlock() 
    public fun tryLock(): Boolean
}

/**
 * Cross-platform condition variable
 */
public interface RealmCondition {
    public fun wait(mutex: RealmMutex)
    public fun wait(mutex: RealmMutex, timeoutMillis: Long): Boolean
    public fun signal()
    public fun signalAll()
}

/**
 * Cross-platform read-write lock
 */
public interface RealmReadWriteLock {
    public fun readLock(): RealmMutex
    public fun writeLock(): RealmMutex
}