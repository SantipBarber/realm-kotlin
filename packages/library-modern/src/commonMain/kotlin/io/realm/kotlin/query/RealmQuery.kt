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

package io.realm.kotlin.query

import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.flow.Flow

/**
 * Query interface for querying objects in a Realm.
 */
public interface RealmQuery<T : BaseRealmObject> {

    /**
     * Finds all objects that satisfy the query.
     */
    public fun find(): RealmResults<T>

    /**
     * Finds the first object that satisfies the query.
     */
    public fun first(): RealmSingleQuery<T>

    /**
     * Limits the number of results returned by the query.
     */
    public fun limit(limit: Int): RealmQuery<T>

    /**
     * Sorts the query results by the given property.
     */
    public fun sort(property: String, sortOrder: Sort = Sort.ASCENDING): RealmQuery<T>

    /**
     * Returns the number of objects that match the query.
     */
    public fun count(): RealmScalarQuery<Long>

    /**
     * Observes changes to the query results.
     */
    public fun asFlow(): Flow<ResultsChange<T>>
}

/**
 * Query interface for single object results.
 */
public interface RealmSingleQuery<T : BaseRealmObject> {
    /**
     * Finds the object or returns null if not found.
     */
    public fun find(): T?
}

/**
 * Query interface for scalar results.
 */
public interface RealmScalarQuery<T> {
    /**
     * Finds the scalar result.
     */
    public fun find(): T
}

/**
 * Enum representing sort order.
 */
public enum class Sort {
    ASCENDING,
    DESCENDING
}