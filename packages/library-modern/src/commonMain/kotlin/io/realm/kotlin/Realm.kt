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

import io.realm.kotlin.internal.ModernRealmImpl
import io.realm.kotlin.notifications.RealmChange
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * A Realm instance represents a connection to a Realm database and is the main entry point for
 * interacting with persisted objects in the database.
 */
public interface Realm : TypedRealm {

    /**
     * The configuration used to create this Realm instance.
     */
    public val configuration: RealmConfiguration

    /**
     * The schema version of this Realm.
     */
    public val version: Long

    /**
     * Checks if the Realm is currently being written to.
     */
    public val isInTransaction: Boolean

    /**
     * Checks if the Realm instance is closed.
     */
    public val isClosed: Boolean

    /**
     * Executes a write transaction in a suspendable manner.
     */
    public suspend fun <R> write(block: MutableRealm.() -> R): R

    /**
     * Executes a write transaction in a blocking manner.
     */
    public fun <R> writeBlocking(block: MutableRealm.() -> R): R

    /**
     * Copies the current Realm to a new file at the specified configuration.
     */
    public suspend fun writeCopyTo(targetConfiguration: RealmConfiguration)

    /**
     * Observes changes to the Realm.
     */
    public fun asFlow(): Flow<RealmChange<Realm>>

    /**
     * Closes the Realm instance.
     */
    public fun close()

    public companion object {
        /**
         * Opens a Realm instance with the specified configuration.
         */
        public fun open(configuration: RealmConfiguration): Realm {
            return ModernRealmImpl(configuration)
        }

        /**
         * Deletes the Realm file at the specified configuration.
         */
        public fun deleteRealm(configuration: RealmConfiguration) {
            // TODO: Implement actual deletion
        }

        /**
         * Compacts the Realm file at the specified configuration.
         */
        public fun compactRealm(configuration: RealmConfiguration): Boolean {
            // TODO: Implement actual compaction
            return true
        }
    }
}