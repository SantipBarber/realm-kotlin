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

import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.BaseRealmObject
import kotlin.reflect.KClass

/**
 * Base interface for all Realm instances.
 */
public interface BaseRealm {

    /**
     * The schema for this Realm instance.
     */
    public val schema: RealmSchema

    /**
     * Returns the number of objects of the given type in the Realm.
     */
    public fun <T : BaseRealmObject> count(clazz: KClass<T>): Long
}