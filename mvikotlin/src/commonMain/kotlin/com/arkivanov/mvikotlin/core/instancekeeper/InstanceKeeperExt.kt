package com.arkivanov.mvikotlin.core.instancekeeper

import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.Store

/**
 * Either returns a currently retained instance or creates (and retains) a new one.
 *
 * See [InstanceKeeper] for more information.
 *
 * @param factory a factory function, accepts the [InstanceKeeper]'s [Lifecycle],
 * called when there is no retained instance yet
 * @return either a currently retained instance or a new one
 */
inline fun <T : Any> InstanceKeeper<T>.getOrCreate(factory: (Lifecycle) -> T): T {
    check(lifecycle.state != Lifecycle.State.DESTROYED) { "The InstanceKeeper is already destroyed" }

    return instance ?: factory(lifecycle).also { instance = it }
}

fun <T : Store<*, *, *>> InstanceKeeper<T>.getOrCreateStore(factory: () -> T): T =
    getOrCreate { lifecycle ->
        val store = factory()
        lifecycle.doOnDestroy(store::dispose)
        store
    }
