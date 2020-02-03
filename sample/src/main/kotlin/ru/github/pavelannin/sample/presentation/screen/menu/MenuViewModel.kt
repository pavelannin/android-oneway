package ru.github.pavelannin.sample.presentation.screen.menu

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import io.reactivex.disposables.SerialDisposable
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject
import ru.github.pavelannin.oneway.OneWay
import ru.github.pavelannin.oneway.Transformation
import ru.github.pavelannin.oneway.create
import ru.github.pavelannin.sample.common.extensions.filterNot
import ru.github.pavelannin.sample.common.extensions.subscribe
import ru.github.pavelannin.sample.common.livedata.toSingle
import ru.github.pavelannin.oneway.syncReduce
import ru.github.pavelannin.oneway.transform
import ru.github.pavelannin.sample.R

class MenuViewModel : ViewModel() {

    val actionSubject: Subject<Action> = UnicastSubject.create()

    private val _state: MutableLiveData<State> = MutableLiveData()
    private val disposable: SerialDisposable = SerialDisposable()

    val navigationState: LiveData<Consumable<NavDirections>> = _state.map(State::navigation)
        .map<State.Navigation, Consumable<NavDirections>>(State.Navigation::action)
        .filterNot(Consumable<NavDirections>::isConsumed)
        .toSingle()

    init {
        OneWay.create(
            initialState = State(),
            actionSubject = actionSubject,
            reducer = syncReduce { action ->
                when (action) {
                    Action.CountPressed -> navigation(R.id.navigationRootMenuToCounterAction)
                    Action.PaginationPressed -> navigation(R.id.navigationRootMenuToPaginationAction)
                    Action.DynamicAnimationPressed -> navigation(R.id.navigationRootMenuToDynamicAnimationAction)
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

    private fun navigation(@IdRes navigationId: Int): Transformation<State> = transform { state ->
        state.copy(
            navigation = state.navigation.copy(
                action = state.navigation.action.copy(
                    value = ActionOnlyNavDirections(navigationId)
                )
            )
        )
    }

    enum class Action { CountPressed, PaginationPressed, DynamicAnimationPressed }

    data class State(val navigation: Navigation = Navigation()) {
        data class Navigation(val action: Consumer<NavDirections> = Consumer(value = null))
    }

    interface Consumable<T> {
        val isConsumed: Boolean
        fun consume(): T
    }

    data class Consumer<T : Any>(@set:Synchronized private var value: T?) : Consumable<T> {

        override val isConsumed: Boolean
            get() = value == null

        override fun consume(): T {
            return checkNotNull(value).also { value = null }
        }
    }
}
