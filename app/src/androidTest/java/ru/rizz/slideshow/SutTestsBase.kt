package ru.rizz.slideshow

import io.mockk.*
import io.mockk.impl.annotations.*
import org.junit.*

abstract class SutTestsBase<TSut : Any> protected constructor() {
	@InjectMockKs protected lateinit var mSut: TSut; private set

	@Before
	fun baseSetUpBaseSutTests() {
		beforeMocksInited()
		MockKAnnotations.init(this, relaxed = true)
		afterMocksInited()
	}

	protected open fun beforeMocksInited() = Unit
	protected open fun afterMocksInited() = Unit

}