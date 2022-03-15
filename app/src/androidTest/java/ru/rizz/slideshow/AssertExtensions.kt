package ru.rizz.slideshow

import kotlinx.coroutines.*
import org.assertj.core.api.*

inline fun <reified T : Throwable> assertThrows(noinline block: () -> Unit): AbstractThrowableAssert<*, *> =
	Assertions.assertThatThrownBy {
		block()
	}.isExactlyInstanceOf(T::class.java)

inline fun <reified T : Throwable> coAssertThrows(noinline block: suspend () -> Unit): AbstractThrowableAssert<*, *> =
	Assertions.assertThatThrownBy {
		runBlocking { block() }
	}.isExactlyInstanceOf(T::class.java)