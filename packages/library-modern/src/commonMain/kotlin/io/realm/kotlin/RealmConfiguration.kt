/*
 * Copyright 2024 Realm Inc.
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

package io.realm.kotlin

import kotlin.reflect.KClass

/**
 * Configuration interface for Realm instances.
 */
public interface Configuration {
    /**
     * The name of the Realm file.
     */
    public val name: String

    /**
     * The path where the Realm file is stored.
     */
    public val path: String

    /**
     * The schema version of the Realm.
     */
    public val schemaVersion: Long

    /**
     * The set of classes that make up the schema.
     */
    public val schema: Set<KClass<out Any>>
}

/**
 * Configuration for local Realm instances.
 */
public interface RealmConfiguration : Configuration {
    /**
     * The encryption key for the Realm file.
     */
    public val encryptionKey: ByteArray?

    /**
     * Callback invoked when the Realm should be compacted on launch.
     */
    public val compactOnLaunchCallback: CompactOnLaunchCallback?

    /**
     * Callback invoked with initial data when the Realm is first created.
     */
    public val initialDataCallback: InitialDataCallback?

    /**
     * Whether to delete the Realm file if a migration is needed but no migration block is provided.
     */
    public val deleteRealmIfMigrationNeeded: Boolean

    /**
     * Builder for creating RealmConfiguration instances.
     */
    public class Builder {
        private var name: String = "default.realm"
        private var schema: Set<KClass<out Any>> = emptySet()
        private var schemaVersion: Long = 0L
        private var encryptionKey: ByteArray? = null
        private var compactOnLaunchCallback: CompactOnLaunchCallback? = null
        private var initialDataCallback: InitialDataCallback? = null
        private var deleteRealmIfMigrationNeeded: Boolean = false

        /**
         * Sets the name of the Realm file.
         */
        public fun name(name: String): Builder = apply {
            this.name = name
        }

        /**
         * Sets the schema classes for the Realm.
         */
        public fun schema(vararg classes: KClass<out Any>): Builder = apply {
            this.schema = classes.toSet()
        }

        /**
         * Sets the schema version for the Realm.
         */
        public fun schemaVersion(version: Long): Builder = apply {
            this.schemaVersion = version
        }

        /**
         * Sets the encryption key for the Realm.
         */
        public fun encryptionKey(key: ByteArray): Builder = apply {
            this.encryptionKey = key
        }

        /**
         * Sets the compact on launch callback.
         */
        public fun compactOnLaunch(callback: CompactOnLaunchCallback): Builder = apply {
            this.compactOnLaunchCallback = callback
        }

        /**
         * Sets the initial data callback.
         */
        public fun initialData(callback: InitialDataCallback): Builder = apply {
            this.initialDataCallback = callback
        }

        /**
         * Sets whether to delete the Realm if migration is needed.
         */
        public fun deleteRealmIfMigrationNeeded(delete: Boolean = true): Builder = apply {
            this.deleteRealmIfMigrationNeeded = delete
        }

        /**
         * Builds the RealmConfiguration.
         */
        public fun build(): RealmConfiguration {
            return RealmConfigurationImpl(
                name = name,
                schema = schema,
                schemaVersion = schemaVersion,
                encryptionKey = encryptionKey,
                compactOnLaunchCallback = compactOnLaunchCallback,
                initialDataCallback = initialDataCallback,
                deleteRealmIfMigrationNeeded = deleteRealmIfMigrationNeeded
            )
        }
    }
}

/**
 * Callback for determining when to compact a Realm on launch.
 */
public fun interface CompactOnLaunchCallback {
    /**
     * Determines whether the Realm should be compacted.
     */
    public fun shouldCompact(totalBytes: Long, usedBytes: Long): Boolean
}

/**
 * Callback for providing initial data to a newly created Realm.
 */
public fun interface InitialDataCallback {
    /**
     * Provides initial data to the Realm.
     */
    public fun write(realm: MutableRealm)
}

/**
 * Internal implementation of RealmConfiguration.
 */
internal data class RealmConfigurationImpl(
    override val name: String,
    override val schema: Set<KClass<out Any>>,
    override val schemaVersion: Long,
    override val encryptionKey: ByteArray?,
    override val compactOnLaunchCallback: CompactOnLaunchCallback?,
    override val initialDataCallback: InitialDataCallback?,
    override val deleteRealmIfMigrationNeeded: Boolean
) : RealmConfiguration {
    
    override val path: String
        get() = name // Simplified for now
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RealmConfigurationImpl) return false
        
        if (name != other.name) return false
        if (schema != other.schema) return false
        if (schemaVersion != other.schemaVersion) return false
        if (encryptionKey != null) {
            if (other.encryptionKey == null) return false
            if (!encryptionKey.contentEquals(other.encryptionKey)) return false
        } else if (other.encryptionKey != null) return false
        if (deleteRealmIfMigrationNeeded != other.deleteRealmIfMigrationNeeded) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + schema.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + (encryptionKey?.contentHashCode() ?: 0)
        result = 31 * result + deleteRealmIfMigrationNeeded.hashCode()
        return result
    }
}