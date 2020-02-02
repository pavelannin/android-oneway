package ru.github.pavelannin.oneway.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class NotNullMediatorLiveData<T> internal constructor() : MediatorLiveData<T>()

fun <T> LiveData<T?>.filterNotNull(): NotNullMediatorLiveData<T> {
    return NotNullMediatorLiveData<T>().also { notNullLiveData ->
        notNullLiveData.addSource(this) { it?.let { notNullLiveData.value = it } }
    }
}