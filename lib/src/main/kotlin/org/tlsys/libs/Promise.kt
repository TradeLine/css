package org.tlsys.libs

/*
external
class Promise<V> {

    constructor(resolve: (V) -> Unit, reject: (Throwable) -> Unit)

    constructor(f: (resolve: (V) -> Unit, reject: (Throwable) -> Unit) -> Unit)

    companion object {
        fun <V> resolve(value: V): Promise<V>
        fun <V> reject(reason: Throwable): Promise<V>
        fun <T> all(vararg args: Promise<T>): Promise<T>
    }

    fun then(resolve: (V) -> Unit): Promise<V>
    fun then(resolve: (V) -> Unit, reject: (Throwable) -> Unit): Promise<V>
    fun catch(f: (Throwable) -> Unit): Promise<V>
}

*/