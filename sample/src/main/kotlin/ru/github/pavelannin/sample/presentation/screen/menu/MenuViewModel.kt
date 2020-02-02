package ru.github.pavelannin.sample.presentation.screen.menu

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import io.reactivex.disposables.SerialDisposable
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject
import ru.github.pavelannin.oneway.OneWay
import ru.github.pavelannin.oneway.Transformation
import ru.github.pavelannin.oneway.create
import ru.github.pavelannin.oneway.lifecycle.filterNotNull
import ru.github.pavelannin.oneway.lifecycle.map
import ru.github.pavelannin.oneway.lifecycle.subscribe
import ru.github.pavelannin.oneway.lifecycle.toSingle
import ru.github.pavelannin.oneway.syncReduce
import ru.github.pavelannin.oneway.transformation
import ru.github.pavelannin.sample.R

class MenuViewModel : ViewModel() {

    val actionSubject: Subject<Action> = UnicastSubject.create()

    private val _state: MutableLiveData<State> = MutableLiveData()
    private val disposable: SerialDisposable = SerialDisposable()

    val navigationState: LiveData<NavDirections> = _state.map(State::navigation)
        .map(State.Navigation::navigationTo)
        .filterNotNull()
        .toSingle()

    init {
        OneWay.create(
            initialState = State(),
            actionSubject = actionSubject,
            reducer = syncReduce { action ->
                when (action) {
                    Action.CountPressed -> navigationTransformation(R.id.navigationRootMenuToCounterAction)
                    Action.PaginationPressed -> navigationTransformation(R.id.navigationRootMenuToPaginationAction)
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

    private fun navigationTransformation(@IdRes navigationId: Int): Transformation<State> = transformation { state ->
        state.copy(navigation = state.navigation.copy(navigationTo = ActionOnlyNavDirections(navigationId)))
    }

    enum class Action { CountPressed, PaginationPressed }

    data class State(val navigation: Navigation = Navigation()) {
        data class Navigation(val navigationTo: NavDirections? = null)
    }
}