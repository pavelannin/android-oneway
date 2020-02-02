package ru.github.pavelannin.oneway

interface Transformation<State> : (State) -> State

inline fun <State> transformation(crossinline block: (State) -> State): Transformation<State> = object : Transformation<State> {
    override fun invoke(state: State): State {
        return block(state)
    }
}

fun <State> noChange(): Transformation<State> = object : Transformation<State> {
    override fun invoke(state: State): State {
        return state
    }
}
