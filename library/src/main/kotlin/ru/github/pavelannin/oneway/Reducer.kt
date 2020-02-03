/*
 *    Copyright 2020 Pavel Annin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
