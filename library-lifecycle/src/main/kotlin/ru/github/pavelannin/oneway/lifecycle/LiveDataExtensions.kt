package ru.github.pavelannin.oneway.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.UnicastSubject

fun <Input, Output> LiveData<Input>.map(
    transform: (Input) -> Output
): LiveData<Output> = Transformations.map(this, transform)

fun <Input, Output> LiveData<Input>.switchMap(
    transform: (Input) -> LiveData<Output>
): LiveData<Output> = Transformations.switchMap(this, transform)

fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> = Transformations.distinctUntilChanged(this)

fun <T> LiveData<T>.filter(predicate: (T) -> Boolean): LiveData<T> {
    return MediatorLiveData<T>().also { filterLiveData ->
        filterLiveData.addSource(this) {
            if (predicate(it)) {
                filterLiveData.value = it
            }
        }
    }
}

fun <Input, Output> LiveData<Input>.map(
    transform: (Input) -> Output,
    scheduler: Scheduler = Schedulers.computation()
): LiveData<Output> {
    return object : LiveData<Output>(), Observer<Input> {

        private var disposable: Disposable? = null
        private val viewStateSubject: UnicastSubject<Input> by lazy { UnicastSubject.create<Input>() }

        override fun onActive() {
            super.onActive()
            disposable = viewStateSubject.map(transform)
                .subscribeOn(scheduler)
                .subscribe { value -> postValue(value) }
            observeForever(this)
        }

        override fun onInactive() {
            super.onInactive()
            removeObserver(this)
            disposable?.dispose()
        }

        override fun onChanged(input: Input) {
            viewStateSubject.onNext(input)
        }
    }
}
