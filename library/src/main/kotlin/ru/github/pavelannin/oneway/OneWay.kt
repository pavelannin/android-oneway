package ru.github.pavelannin.oneway

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

object OneWay

@Suppress(names = ["unused"])
fun <State, Action> OneWay.create(
    initialState: State,
    actionSubjects: List<Subject<out Action>>,
    reducer: Reducer<Action, State>,
    scheduler: Scheduler = Schedulers.computation()
): Observable<State> = Observable.defer {
    val latestStateSubject: BehaviorSubject<State> = BehaviorSubject.create()

    return@defer Observable.merge(actionSubjects)
        .withLatestFrom<State, Observable<Transformation<State>>>(
            latestStateSubject,
            BiFunction { action, state -> reducer(action, state) }
        )
        .flatMap { it.onErrorResumeNext(Observable.empty()) }
        .scan(initialState) { state, transformation -> transformation(state) }
        .startWith(initialState)
        .distinctUntilChanged()
        .doOnNext(latestStateSubject::onNext)
        .subscribeOn(scheduler)
}

fun <State, Action> OneWay.create(
    initialState: State,
    actionSubject: Subject<out Action>,
    reducer: Reducer<Action, State>,
    scheduler: Scheduler = Schedulers.computation()
): Observable<State> {
    return create(initialState, listOf(actionSubject), reducer, scheduler)
}