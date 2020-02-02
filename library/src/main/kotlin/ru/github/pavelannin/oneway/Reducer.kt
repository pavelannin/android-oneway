package ru.github.pavelannin.oneway

import io.reactivex.Observable

interface Reducer<in Action, State> : (Action, State) -> Observable<Transformation<State>>

inline fun <Action, State> reduce(
    crossinline block: (Action) -> Observable<Transformation<State>>
): Reducer<Action, State> = object : Reducer<Action, State> {
    override fun invoke(action: Action, state: State): Observable<Transformation<State>> = block(action)
}

inline fun <Action, State> reduceWithState(
    crossinline block: (Action, State) -> Observable<Transformation<State>>
): Reducer<Action, State> = object : Reducer<Action, State> {
    override fun invoke(action: Action, state: State): Observable<Transformation<State>> = block(action, state)
}

inline fun <Action, State> syncReduce(
    crossinline block: (Action) -> Transformation<State>
): Reducer<Action, State> = object : Reducer<Action, State> {
    override fun invoke(action: Action, state: State): Observable<Transformation<State>> = Observable.just(block(action))
}

inline fun <Action, State> syncReduceWithState(
    crossinline block: (Action, State) -> Transformation<State>
): Reducer<Action, State> = object : Reducer<Action, State> {
    override fun invoke(action: Action, state: State): Observable<Transformation<State>> = Observable.just(block(action, state))
}
