package ru.rizz.slideshow

import io.mockk.*

fun verifyOnce(
	ordering: Ordering = Ordering.UNORDERED,
	inverse: Boolean = false,
	timeout: Long = 0,
	verifyBlock: MockKVerificationScope.() -> Unit
) =
	verify(ordering, inverse, exactly = 1, timeout = timeout, verifyBlock = verifyBlock)

fun coVerifyOnce(
	ordering: Ordering = Ordering.UNORDERED,
	inverse: Boolean = false,
	timeout: Long = 0,
	verifyBlock: suspend MockKVerificationScope.() -> Unit
) =
	coVerify(ordering, inverse, exactly = 1, timeout = timeout, verifyBlock = verifyBlock)

//fun verifyNever(
//	ordering: Ordering = Ordering.UNORDERED,
//	inverse: Boolean = false,
//	timeout: Long = 0,
//	verifyBlock: MockKVerificationScope.() -> Unit
//) =
//	verify(ordering, inverse, exactly = 0, timeout = timeout, verifyBlock = verifyBlock)

fun verifyWasNotCalled(vararg mocks: Any) =
	mocks.forEach {
		verify { it wasNot called }
	}