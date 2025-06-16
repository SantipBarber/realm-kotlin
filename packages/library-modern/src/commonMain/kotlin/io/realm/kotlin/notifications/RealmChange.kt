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

package io.realm.kotlin.notifications

/**
 * Base interface for all change notifications.
 */
public interface RealmChange<T> {
    /**
     * The object that has changed.
     */
    public val obj: T
}

/**
 * Represents changes to query results.
 */
public interface ResultsChange<T> : RealmChange<List<T>> {
    /**
     * The list of results that changed.
     */
    public val list: List<T>
    
    /**
     * Information about the specific changes.
     */
    public val changeSet: CollectionChangeSet?
    
    override val obj: List<T>
        get() = list
}

/**
 * Detailed information about changes to a collection.
 */
public interface CollectionChangeSet {
    /**
     * Indices of objects that were deleted.
     */
    public val deletionRanges: List<Range>
    
    /**
     * Indices of objects that were inserted.
     */
    public val insertionRanges: List<Range>
    
    /**
     * Indices of objects that were modified.
     */
    public val changeRanges: List<Range>
}

/**
 * Represents changes to a single object.
 */
public interface ObjectChange<T> {
    /**
     * The object that changed, or null if deleted.
     */
    public val obj: T?
    
    /**
     * Names of fields that changed in this object.
     */
    public val changedFields: List<String>
}

/**
 * Represents a range of indices.
 */
public data class Range(
    public val startIndex: Int,
    public val length: Int
)