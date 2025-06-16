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

import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.interop.RealmResultsHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

/**
 * Modern implementation of RealmResults using Modern Interop Layer.
 */
public class ModernRealmResultsImpl<T : BaseRealmObject>(
    private val clazz: KClass<T>,
    private val resultsHandle: RealmResultsHandle
) : RealmResults<T> {

    override val size: Int
        get() = RealmCoreInterop.getResultsSize(resultsHandle).toInt()

    override fun contains(element: T): Boolean {
        // TODO: Implement proper contains check using object handles
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): T {
        val objectHandle = RealmCoreInterop.getResultsObject(resultsHandle, index.toLong())
            ?: throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")
        
        // Create proper proxy object from handle using modern proxy system
        return io.realm.kotlin.modern.proxy.RealmObjectProxyFactory.createProxy(clazz, objectHandle)
    }

    override fun indexOf(element: T): Int {
        // TODO: Implement proper indexOf using object handles
        return -1
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<T> {
        return RealmResultsIterator()
    }

    override fun lastIndexOf(element: T): Int {
        // TODO: Implement proper lastIndexOf using object handles  
        return -1
    }

    override fun listIterator(): ListIterator<T> {
        return listIterator(0)
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return RealmResultsListIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
        }
        return (fromIndex until toIndex).map { get(it) }
    }

    override fun asFlow(): Flow<ResultsChange<T>> {
        return flowOf()
    }
    
    private fun createRealmObjectProxy(
        clazz: KClass<T>, 
        objectHandle: io.realm.kotlin.modern.interop.RealmObjectHandle
    ): T {
        // Use the modern dynamic proxy system
        return io.realm.kotlin.modern.proxy.RealmObjectProxyFactory.createProxy(clazz, objectHandle)
    }
    
    private inner class RealmResultsIterator : Iterator<T> {
        private var currentIndex = 0
        
        override fun hasNext(): Boolean = currentIndex < size
        
        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return get(currentIndex++)
        }
    }
    
    private inner class RealmResultsListIterator(
        private var currentIndex: Int
    ) : ListIterator<T> {
        
        override fun hasNext(): Boolean = currentIndex < size
        
        override fun hasPrevious(): Boolean = currentIndex > 0
        
        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return get(currentIndex++)
        }
        
        override fun nextIndex(): Int = currentIndex
        
        override fun previous(): T {
            if (!hasPrevious()) throw NoSuchElementException()
            return get(--currentIndex)
        }
        
        override fun previousIndex(): Int = currentIndex - 1
    }
}