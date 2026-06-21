package android.util

import java.util.TreeMap

class SparseArray<E> : Cloneable {
    private val map = TreeMap<Int, E>()

    fun put(key: Int, value: E) {
        map[key] = value
    }

    fun get(key: Int): E? = map[key]

    fun get(key: Int, valueIfKeyNotFound: E): E = map.getOrDefault(key, valueIfKeyNotFound)
}
