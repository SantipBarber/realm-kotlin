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

import io.realm.kotlin.types.BaseRealmObject
import kotlin.reflect.KClass

/**
 * Interface representing a writable Realm transaction.
 */
public interface MutableRealm : TypedRealm {

    /**
     * Creates a new object of the given type.
     */
    public fun <T : BaseRealmObject> create(clazz: KClass<T>): T

    /**
     * Creates a new object of the given type with the specified primary key.
     */
    public fun <T : BaseRealmObject> create(
        clazz: KClass<T>,
        primaryKey: Any?
    ): T

    /**
     * Copies an object to the Realm.
     */
    public fun <T : BaseRealmObject> copyToRealm(
        obj: T,
        updatePolicy: UpdatePolicy = UpdatePolicy.ERROR
    ): T

    /**
     * Deletes an object from the Realm.
     */
    public fun delete(obj: BaseRealmObject)

    /**
     * Deletes all objects of the given type.
     */
    public fun <T : BaseRealmObject> delete(clazz: KClass<T>)

    /**
     * Cancels the current transaction.
     */
    public fun cancelWrite()
}

/**
 * Enum representing different update policies for objects.
 */
public enum class UpdatePolicy {
    ERROR,
    ALL
}