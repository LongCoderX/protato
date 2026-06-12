package com.protato.app.state

import com.protato.app.AppState

internal fun reduceTodoEvent(
    state: AppState,
    event: TodoProtatoEvent
): StateReduction {
    return when (event) {
        is ProtatoEvent.AddTodo -> {
            StateReduction(state.copy(todos = listOf(event.todo) + state.todos))
        }

        is ProtatoEvent.AddTodos -> {
            StateReduction(
                state = if (event.todos.isEmpty()) {
                    state
                } else {
                    state.copy(todos = event.todos + state.todos)
                }
            )
        }

        is ProtatoEvent.ToggleTodo -> {
            val nextCompleted = !event.todo.completed
            StateReduction(
                state = state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == event.todo.id) todo.copy(completed = nextCompleted) else todo
                    }
                ),
                result = ProtatoEventResult(
                    effects = if (nextCompleted) {
                        listOf(
                            ProtatoEffect.CancelTodoReminder(event.todo.id),
                            ProtatoEffect.PlayTodoCompletedSound
                        )
                    } else {
                        emptyList()
                    }
                )
            )
        }

        is ProtatoEvent.DeleteTodo -> {
            StateReduction(
                state = state.copy(todos = state.todos.filterNot { it.id == event.todo.id }),
                result = ProtatoEventResult(
                    effects = listOf(
                        ProtatoEffect.CancelTodoReminder(event.todo.id),
                        ProtatoEffect.RemoveTodoSelection(event.todo.id)
                    )
                )
            )
        }

        is ProtatoEvent.DeleteTodos -> {
            StateReduction(
                state = state.copy(todos = state.todos.filterNot { it.id in event.todoIds }),
                result = ProtatoEventResult(
                    effects = event.todoIds.flatMap { todoId ->
                        listOf(
                            ProtatoEffect.CancelTodoReminder(todoId),
                            ProtatoEffect.RemoveTodoSelection(todoId)
                        )
                    }
                )
            )
        }

        is ProtatoEvent.UpdateTodo -> {
            StateReduction(
                state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == event.todo.id) event.todo else todo
                    }
                )
            )
        }
    }
}
