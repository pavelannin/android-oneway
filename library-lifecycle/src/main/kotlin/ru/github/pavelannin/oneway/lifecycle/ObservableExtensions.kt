package ru.github.pavelannin.oneway.lifecycle

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

fun <T> Observable<T>.subscribe(liveData: MutableLiveData<T>): Disposable {
    return subscribe { value -> liveData.postValue(value) }
}