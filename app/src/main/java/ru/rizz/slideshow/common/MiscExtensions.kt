package ru.rizz.slideshow.common

import androidx.lifecycle.*
import kotlin.time.*

//fun String?.isNotNullNorEmpty(): Boolean {
//	contract { returns(true) implies (this@isNotNullNorEmpty != null) }
//	return !isNullOrEmpty()
//}

val Duration.seconds get() = toInt(DurationUnit.SECONDS)

val Throwable.msg get() = message ?: ""

val <T> LiveData<T>.require get() = value!!