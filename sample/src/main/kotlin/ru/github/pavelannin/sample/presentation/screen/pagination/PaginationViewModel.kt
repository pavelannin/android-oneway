@file:Suppress("UNREACHABLE_CODE")

package ru.github.pavelannin.sample.presentation.screen.pagination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject
import ru.github.pavelannin.oneway.OneWay
import ru.github.pavelannin.oneway.Transformation
import ru.github.pavelannin.oneway.create
import ru.github.pavelannin.oneway.lifecycle.subscribe
import ru.github.pavelannin.oneway.reduceWithState
import ru.github.pavelannin.oneway.transformation
import java.util.concurrent.TimeUnit

class PaginationViewModel(
    private val api: ApiMock = ApiMock()
) : ViewModel() {

    val actionSubject: Subject<ExternalAction> = UnicastSubject.create()
    val state: LiveData<State> get() = _state

    private val internalActionSubject: Subject<InternalAction> = UnicastSubject.create()
    private val _state: MutableLiveData<State> = MutableLiveData()
    private val disposable: SerialDisposable = SerialDisposable()

    init {
        OneWay.create<State, Action>(
            initialState = State(),
            actionSubjects = listOf(actionSubject, internalActionSubject),
            reducer = reduceWithState { action, state ->
                when (action) {
                    is InternalAction -> when (action) {
                        InternalAction.InitialLoad -> fetchData(state)
                    }
                    is ExternalAction -> when (action) {
                        ExternalAction.LoadMore -> if (state.loadingStatus is State.Response.Success) fetchData(state) else Observable.empty()
                    }
                    else -> error("Unknown action type ($action)")
                }
            }
        )
            .subscribe(_state)
            .also { disposable.set(it) }

        internalActionSubject.onNext(InternalAction.InitialLoad)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    private fun fetchData(oldState: State): Observable<Transformation<State>> {
        return api.anotherRequest(oldState.nextPage)
            .map { range ->
                transformation<State> { state ->
                    state.copy(
                        data = state.data.plus(range.toList()),
                        nextPage = state.nextPage.inc(),
                        loadingStatus = State.Response.Success(Unit)
                    )
                }
            }
            .onErrorReturn { error ->
                transformation { state -> state.copy(loadingStatus = State.Response.Failure(error)) }
            }
            .toObservable()
            .startWith(transformation { state -> state.copy(loadingStatus = State.Response.Loading()) })
    }

    private interface Action

    enum class ExternalAction : Action { LoadMore }

    private enum class InternalAction : Action { InitialLoad }

    data class State(
        val data: List<Int> = emptyList(),
        val nextPage: Int = 0,
        val loadingStatus: Response<Unit>? = null
    ) {

        sealed class Response<out T> {
            class Loading<out T> : Response<T>()
            data class Success<out T>(val value: T) : Response<T>()
            data class Failure<out T>(val error: Throwable) : Response<T>()
        }
    }
}

class ApiMock {

    fun anotherRequest(page: Int, limit: Int = 20): Single<IntRange> {
        val offset = page * limit
        return Single.just((offset until offset + limit))
            .delay(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
    }
}
