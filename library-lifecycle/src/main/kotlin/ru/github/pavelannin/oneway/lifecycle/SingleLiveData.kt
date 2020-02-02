package ru.github.pavelannin.oneway.lifecycle

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveData<T> : MediatorLiveData<T>() {

    private val observers: MutableSet<ObserverWrapper<in T>> by lazy { mutableSetOf<ObserverWrapper<in T>>() }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observeForever(wrapper)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        if (observers.remove(observer)) {
            super.removeObserver(observer)
            return
        }

        observers.firstOrNull { it.observer == observer }
            ?.also { wrapper ->
                observers.remove(wrapper)
                super.removeObserver(wrapper)
            }
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.newValue() }
        super.setValue(t)
    }

    private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {

        private val pending: AtomicBoolean = AtomicBoolean(false)

        override fun onChanged(t: T?) {
            if (pending.getAndSet(false)) {
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending.set(true)
        }
    }
}

fun <T> LiveData<T>.toSingle(): LiveData<T> {
    return SingleLiveData<T>().also { singleLiveData ->
        singleLiveData.addSource(this) { singleLiveData.value = it }
    }
}
