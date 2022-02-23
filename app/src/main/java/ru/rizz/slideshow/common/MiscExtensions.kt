package ru.rizz.slideshow.common

import kotlin.contracts.*
import kotlin.time.*

val Duration.seconds get() = toInt(DurationUnit.SECONDS)

//fun String?.isNotNullNorEmpty(): Boolean {
//	contract { returns(true) implies (this@isNotNullNorEmpty != null) }
//	return !isNullOrEmpty()
//}

val Throwable.msg get() = message ?: ""