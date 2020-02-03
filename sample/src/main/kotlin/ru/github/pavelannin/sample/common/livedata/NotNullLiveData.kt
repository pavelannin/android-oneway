package ru.github.pavelannin.sample.common.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class NotNullMediatorLiveData<T> : MediatorLiveData<T>()

fun <T> LiveData<T?>.filterNotNull(): NotNullMediatorLiveData<T> {
    return NotNullMediatorLiveData<T>().also { notNullLiveData ->
        notNullLiveData.addSource(this) { it?.let { notNullLiveData.value = it } }
    }
}
