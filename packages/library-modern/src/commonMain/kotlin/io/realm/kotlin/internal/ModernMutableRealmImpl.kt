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
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.interop.RealmTransactionHandle
import kotlin.reflect.KClass

/**
 * Modern implementation of the MutableRealm interface using Modern Interop Layer.
 */
public class ModernMutableRealmImpl(
    private val realm: ModernRealmImpl,
    private val transactionHandle: RealmTransactionHandle
) : MutableRealm {

    override val schema: RealmSchema = realm.schema

    override fun <T : BaseRealmObject> create(clazz: KClass<T>): T {
        return create(clazz, null)
    }

    override fun <T : BaseRealmObject> create(
        clazz: KClass<T>,
        primaryKey: Any?
    ): T {
        val className = clazz.simpleName ?: "RealmObject"
        val objectHandle = RealmCoreInterop.createObject(transactionHandle, className, primaryKey)
        
        // Create a proxy object that represents the created Realm object
        @Suppress("UNCHECKED_CAST")
        return createRealmObjectProxy(clazz, objectHandle) as T
    }

    override fun <T : BaseRealmObject> copyToRealm(
        obj: T,
        updatePolicy: UpdatePolicy
    ): T {
        // Create a new object and copy properties
        val className = obj::class.simpleName ?: "RealmObject"
        val objectHandle = RealmCoreInterop.createObject(transactionHandle, className)
        
        // TODO: Copy all properties from obj to the new object
        // This would require reflection or code generation in a real implementation
        
        @Suppress("UNCHECKED_CAST")
        return createRealmObjectProxy(obj::class as KClass<T>, objectHandle)
    }

    override fun delete(obj: BaseRealmObject) {
        // TODO: Get object handle from proxy and delete it
        // val objectHandle = getObjectHandle(obj)
        // RealmCoreInterop.deleteObject(transactionHandle, objectHandle)
    }

    override fun <T : BaseRealmObject> delete(clazz: KClass<T>) {
        // TODO: Delete all objects of this class
        // This would require querying all objects and deleting them
    }

    override fun cancelWrite() {
        RealmCoreInterop.cancelWrite(transactionHandle)
    }

    override fun <T : BaseRealmObject> query(
        clazz: KClass<T>,
        query: String,
        vararg args: Any?
    ): RealmQuery<T> {
        return realm.query(clazz, query, *args)
    }

    override fun <T : BaseRealmObject> findLatest(obj: T): T? {
        return realm.findLatest(obj)
    }

    override fun <T : BaseRealmObject> count(clazz: KClass<T>): Long {
        return realm.count(clazz)
    }
    
    private fun <T : BaseRealmObject> createRealmObjectProxy(
        clazz: KClass<T>, 
        objectHandle: io.realm.kotlin.modern.interop.RealmObjectHandle
    ): T {
        // Use the modern dynamic proxy system
        return io.realm.kotlin.modern.proxy.RealmObjectProxyFactory.createProxy(clazz, objectHandle)
    }
}