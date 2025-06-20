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

package io.realm.kotlin.types

/**
 * Base interface for all objects that can be persisted in a Realm.
 */
public interface BaseRealmObject {
    
    /**
     * Checks if this object is managed by a Realm.
     */
    public fun isManaged(): Boolean
    
    /**
     * Checks if this object is valid (not deleted and still managed).
     */
    public fun isValid(): Boolean
}