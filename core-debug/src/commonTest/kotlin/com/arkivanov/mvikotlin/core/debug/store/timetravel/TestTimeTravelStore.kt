package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.core.debug.store.StoreEventType
import com.arkivanov.mvikotlin.core.debug.store.timetravel.TimeTravelStore.EventDebugger
import com.arkivanov.mvikotlin.core.debug.store.timetravel.TimeTravelStore.EventProcessor
import com.arkivanov.mvikotlin.core.internal.rx.Subject
import com.arkivanov.mvikotlin.core.internal.rx.onComplete
import com.arkivanov.mvikotlin.core.internal.rx.onNext
import com.arkivanov.mvikotlin.core.internal.rx.subscribe
import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.rx.Observer
import com.arkivanov.mvikotlin.utils.internal.AtomicList
import com.arkivanov.mvikotlin.utils.internal.clear
import com.arkivanov.mvikotlin.utils.internal.isEmpty
import com.arkivanov.mvikotlin.utils.internal.plusAssign
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TestTimeTravelStore(
    override val name: String
) : TimeTravelStore<String, String, String> {

    override val eventProcessor = TestEventProcessor()
    override val eventDebugger = TestEventDebugger()

    private val _events = Subject<TimeTravelEvent>()
    override fun events(observer: Observer<TimeTravelEvent>): Disposable = _events.subscribe(observer)

    private val isStateRestored = AtomicBoolean()

    fun sendEvent(event: TimeTravelEvent) {
        _events.onNext(event)
    }

    override fun init() {
    }

    override fun restoreState() {
        isStateRestored.value = true
    }

    override val state: String get() = TODO()

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    override fun states(observer: Observer<String>): Disposable = TODO()

    override fun labels(observer: Observer<String>): Disposable = TODO()

    override fun accept(intent: String) {
    }

    override fun dispose() {
        _isDisposed.value = true
        _events.onComplete()
    }

    fun assertStateRestored() {
        assertTrue(isStateRestored.value)
    }

    class TestEventProcessor : EventProcessor {
        private val events = AtomicList<Pair<StoreEventType, Any>>()

        override fun process(type: StoreEventType, value: Any) {
            events += type to value
        }

        fun assertProcessedEvent(type: StoreEventType, value: Any) {
            val pair = type to value
            assertEquals(1, events.value.count { it == pair })
        }

        fun assertSingleProcessedEvent(type: StoreEventType, value: Any) {
            assertEquals(listOf(type to value), events.value)
        }

        fun assertNoProcessedEvents() {
            assertTrue(events.isEmpty)
        }

        fun reset() {
            events.clear()
        }
    }

    class TestEventDebugger : EventDebugger {
        private val events = AtomicList<TimeTravelEvent>()

        override fun debug(event: TimeTravelEvent) {
            events += event
        }

        fun assertSingleDebuggedEvent(event: TimeTravelEvent) {
            assertEquals(listOf(event), events.value)
        }

        fun assertNoDebuggedEvents() {
            assertTrue(events.isEmpty)
        }
    }
}
