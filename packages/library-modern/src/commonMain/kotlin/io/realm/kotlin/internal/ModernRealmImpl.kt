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

package io.realm.kotlin.internal

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.notifications.RealmChange
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.modern.interop.ModernRealmManager
import io.realm.kotlin.modern.interop.RealmDatabaseHandle
import io.realm.kotlin.modern.interop.RealmCoreInterop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

/**
 * Modern implementation of the Realm interface using Modern Interop Layer.
 */
public class ModernRealmImpl(
    override val configuration: RealmConfiguration
) : Realm {

    private val databaseHandle: RealmDatabaseHandle = ModernRealmManager.openRealm(configuration)

    override val version: Long 
        get() = RealmCoreInterop.getSchemaVersion(databaseHandle)

    override val isInTransaction: Boolean 
        get() = ModernRealmManager.isInTransaction(databaseHandle)

    override val isClosed: Boolean = false

    override val schema: RealmSchema = ModernRealmSchemaImpl(databaseHandle)

    override suspend fun <R> write(block: MutableRealm.() -> R): R {
        return ModernRealmManager.executeWrite(databaseHandle) { transactionHandle ->
            val mutableRealm = ModernMutableRealmImpl(this, transactionHandle)
            mutableRealm.block()
        }
    }

    override fun <R> writeBlocking(block: MutableRealm.() -> R): R {
        return ModernRealmManager.executeWrite(databaseHandle) { transactionHandle ->
            val mutableRealm = ModernMutableRealmImpl(this, transactionHandle)
            mutableRealm.block()
        }
    }

    override suspend fun writeCopyTo(targetConfiguration: RealmConfiguration) {
        // Create backup using modern interop
        val sourcePath = configuration.path ?: configuration.name ?: "default.realm"
        val targetPath = targetConfiguration.path ?: targetConfiguration.name ?: "copy.realm"
        
        // Use platform utils for file operations
        io.realm.kotlin.modern.platform.PlatformUtils.copyFile(sourcePath, targetPath)
    }

    override fun asFlow(): Flow<RealmChange<Realm>> {
        // TODO: Implement using RealmCoreInterop.addChangeListener
        return flowOf()
    }

    override fun close() {
        ModernRealmManager.closeRealm(configuration)
    }

    override fun <T : BaseRealmObject> query(
        clazz: KClass<T>,
        query: String,
        vararg args: Any?
    ): RealmQuery<T> {
        return ModernRealmQueryImpl(clazz, databaseHandle, query, args.toList())
    }

    override fun <T : BaseRealmObject> findLatest(obj: T): T? {
        // TODO: Implement using RealmCoreInterop.findObject
        return obj
    }

    override fun <T : BaseRealmObject> count(clazz: KClass<T>): Long {
        val queryHandle = RealmCoreInterop.createQuery(databaseHandle, clazz.simpleName ?: "RealmObject")
        return RealmCoreInterop.getQueryCount(queryHandle)
    }
}