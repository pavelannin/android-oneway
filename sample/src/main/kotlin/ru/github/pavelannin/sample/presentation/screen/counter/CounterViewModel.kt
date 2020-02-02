package ru.github.pavelannin.sample.presentation.screen.counter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.SerialDisposable
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject
import ru.github.pavelannin.oneway.OneWay
import ru.github.pavelannin.oneway.create
import ru.github.pavelannin.oneway.lifecycle.subscribe
import ru.github.pavelannin.oneway.syncReduce
import ru.github.pavelannin.oneway.transformation

class CounterViewModel : ViewModel() {

    val actionSubject: Subject<Action> = UnicastSubject.create()
    val state: LiveData<State> get() = _state

    private val _state: MutableLiveData<State> = MutableLiveData()
    private val disposable: SerialDisposable = SerialDisposable()

    init {
        OneWay.create(
            initialState = State(),
            actionSubject = actionSubject,
            reducer = syncReduce { action ->
                when (action) {
                    Action.Increment -> transformation { state -> state.copy(count = state.count.inc()) }
                    Action.Decrement -> transformation { state -> state.copy(count = state.count.dec()) }
                }
            }
        )
            .subscribe(_state)
            .also { disposable.set(it) }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    enum class Action { Increment, Decrement }

    data class State(val count: Int = 0)
}
