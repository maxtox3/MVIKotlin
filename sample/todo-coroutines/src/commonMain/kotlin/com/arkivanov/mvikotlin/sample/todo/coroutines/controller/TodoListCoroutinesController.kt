package com.arkivanov.mvikotlin.sample.todo.coroutines.controller

import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.sample.todo.common.controller.TodoListController
import com.arkivanov.mvikotlin.sample.todo.common.controller.TodoListController.Dependencies
import com.arkivanov.mvikotlin.sample.todo.common.controller.TodoListController.Output
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.addEventToAddIntent
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.addLabelToListIntent
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.addStateToAddModel
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.busEventToListIntent
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.listEventToListIntent
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.listEventToOutput
import com.arkivanov.mvikotlin.sample.todo.common.internal.mapper.listStateToListModel
import com.arkivanov.mvikotlin.sample.todo.common.view.TodoAddView
import com.arkivanov.mvikotlin.sample.todo.common.view.TodoListView
import com.arkivanov.mvikotlin.sample.todo.coroutines.eventBus
import com.arkivanov.mvikotlin.sample.todo.coroutines.ioDispatcher
import com.arkivanov.mvikotlin.sample.todo.coroutines.mainDispatcher
import com.arkivanov.mvikotlin.sample.todo.coroutines.mapNotNull
import com.arkivanov.mvikotlin.sample.todo.coroutines.store.TodoAddStoreFactory
import com.arkivanov.mvikotlin.sample.todo.coroutines.store.TodoListStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow

@ExperimentalCoroutinesApi
@FlowPreview
class TodoListCoroutinesController(dependencies: Dependencies) : TodoListController {

    private val todoListStore =
        TodoListStoreFactory(
            storeFactory = dependencies.storeFactory,
            database = dependencies.database,
            mainContext = mainDispatcher,
            ioContext = ioDispatcher
        ).create()

    private val todoAddStore =
        TodoAddStoreFactory(
            storeFactory = dependencies.storeFactory,
            database = dependencies.database,
            mainContext = mainDispatcher,
            ioContext = ioDispatcher
        ).create()

    init {
        bind(dependencies.lifecycle, BinderLifecycleMode.CREATE_DESTROY, mainDispatcher) {
            eventBus.asFlow().mapNotNull(busEventToListIntent) bindTo todoListStore
            todoAddStore.labels.mapNotNull(addLabelToListIntent) bindTo todoListStore
        }

        dependencies.lifecycle.doOnDestroy {
            todoListStore.dispose()
            todoAddStore.dispose()
        }
    }

    override fun onViewCreated(
        todoListView: TodoListView,
        todoAddView: TodoAddView,
        viewLifecycle: Lifecycle,
        output: (Output) -> Unit
    ) {
        bind(viewLifecycle, BinderLifecycleMode.CREATE_DESTROY, mainDispatcher) {
            todoListView.events.mapNotNull(listEventToListIntent) bindTo todoListStore
            todoAddView.events.mapNotNull(addEventToAddIntent) bindTo todoAddStore
        }

        bind(viewLifecycle, BinderLifecycleMode.START_STOP, mainDispatcher) {
            todoListStore.states.mapNotNull(listStateToListModel) bindTo todoListView
            todoAddStore.states.mapNotNull(addStateToAddModel) bindTo todoAddView
            todoListView.events.mapNotNull(listEventToOutput) bindTo { output(it) }
        }
    }
}
