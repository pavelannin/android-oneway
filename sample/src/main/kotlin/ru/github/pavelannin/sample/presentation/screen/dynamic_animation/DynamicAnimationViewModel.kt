package ru.github.pavelannin.sample.presentation.screen.dynamic_animation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.SerialDisposable
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject
import ru.github.pavelannin.oneway.OneWay
import ru.github.pavelannin.oneway.create
import ru.github.pavelannin.sample.common.extensions.subscribe
import ru.github.pavelannin.oneway.syncReduce
import ru.github.pavelannin.oneway.transform

class DynamicAnimationViewModel : ViewModel() {

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
                    Action.Clean -> transform { state -> state.copy(animationEndPoint = State.AnimationEndPoint.Start) }
                    Action.ToTopLeft -> transform { state -> state.copy(animationEndPoint = State.AnimationEndPoint.TopLeft) }
                    Action.ToTopRight -> transform { state -> state.copy(animationEndPoint = State.AnimationEndPoint.TopRight) }
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

    enum class Action { Clean, ToTopLeft, ToTopRight }

    data class State(val animationEndPoint: AnimationEndPoint = AnimationEndPoint.Start) {
        enum class AnimationEndPoint { Start, TopLeft, TopRight }
    }
}
