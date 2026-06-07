package android.util

import java.util.TreeMap

class SparseArray<E> : Cloneable {
    private val map = TreeMap<Int, E>()

    fun put(key: Int, value: E) {
        map[key] = value
    }

    fun get(key: Int): E? {
        return map[key]
    }

    fun get(key: Int, valueIfKeyNotFound: E): E {
        return map.getOrDefault(key, valueIfKeyNotFound)
    }

    fun size(): Int {
        return map.size
    }

    fun keyAt(index: Int): Int {
        return map.keys.elementAt(index)
    }

    fun valueAt(index: Int): E {
        return map.values.elementAt(index)
    }
    
    fun remove(key: Int) {
        map.remove(key)
    }
    
    fun clear() {
        map.clear()
    }
}
