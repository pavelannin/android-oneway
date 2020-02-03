package ru.github.pavelannin.sample.common.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject

fun <T> Observable<T>.subscribe(liveData: MutableLiveData<T>): Disposable {
    return subscribe { value -> liveData.postValue(value) }
}

fun <T> LiveData<T>.filter(predicate: (T) -> Boolean): LiveData<T> {
    return MediatorLiveData<T>().also { filterLiveData ->
        filterLiveData.addSource(this) {
            if (predicate(it)) {
                filterLiveData.value = it
            }
        }
    }
}

fun <T> LiveData<T>.filterNot(predicate: (T) -> Boolean): LiveData<T> {
    return filter { predicate.invoke(it).not() }
}

fun <Input, Output> LiveData<Input>.map(
    transform: (Input) -> Output,
    scheduler: Scheduler = Schedulers.computation()
): LiveData<Output> {
    return object : LiveData<Output>(), Observer<Input> {

        private var disposable: Disposable? = null
        private val viewStateSubject: Subject<Input> by lazy { PublishSubject.create<Input>() }

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
