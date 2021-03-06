package com.arkivanov.mvikotlin.main.store

import com.arkivanov.mvikotlin.core.test.internal.StoreGenericTests
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DefaultStoreTest : StoreGenericTests by StoreGenericTests(
    storeFactory = { initialState, bootstrapper, executorFactory, reducer ->
        DefaultStore(
            initialState = initialState,
            bootstrapper = bootstrapper,
            executorFactory = executorFactory,
            reducer = reducer
        )
    }
) {

    @BeforeTest
    fun before() {
        isAssertOnMainThreadEnabled = false
    }

    @AfterTest
    fun after() {
        isAssertOnMainThreadEnabled = true
    }
}
