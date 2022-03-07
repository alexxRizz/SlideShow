package ru.rizz.slideshow.main.image

import android.net.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.main.image.iterator.*
import ru.rizz.slideshow.settings.*
import kotlin.coroutines.cancellation.*
import kotlin.time.Duration.Companion.seconds

class ImageLoaderTests : SutTestsBase<ImageLoader>() {

	companion object {
		private val TREE_URI = Uri.parse("imagesDirPath")
	}

	@MockK private lateinit var mSettingsRepository: ISettingsReadonlyRepository
	@MockK private lateinit var mImageIterator: IImageIterator
	@MockK private lateinit var mImageCursorFactory: IImageCursorFactory

	@MockK private lateinit var mImageCursor: IImageCursor

	private lateinit var mSettings: Settings

	@Before
	fun setUp() {
		mSettings = newSettings()
		coEvery { mSettingsRepository.getSettings() } returns mSettings
		every { mImageCursorFactory.new(TREE_URI) } returns mImageCursor
		every { mImageCursor.count } returns 3
	}

	@Test
	fun should_no_images_if_settings_is_zero() = runTest {
		coEvery { mSettingsRepository.getSettings() } returns null

		assertThat(mSut.images.count()).isZero
	}

	@Test
	fun should_emit_not_found_error_if_cursor_is_null() = runTest {
		every { mImageCursorFactory.new(TREE_URI) } returns null

		assertLast(ImageLoadingResult.error("В указанной папке нет файлов изображений"))
		verifyWasNotCalled(mImageCursor)
	}

	@Test
	fun should_catch_cancel_exception() = runTest {
		coEvery { mImageIterator.iterate(any(), any(), any(), any()) } throws CancellationException("test")

		mSut.images.toList().assertFirst()
	}

	@Test
	fun should_emit_not_found_error_if_cursor_count_is_zero() = runTest {
		every { mImageCursor.count } returns 0

		assertLast(ImageLoadingResult.error("В указанной папке нет файлов изображений"))
		verifyOnce { mImageCursor.close() }
	}

	@Test
	fun should_emit_error_if_exception_catched() = runTest {
		coEvery { mImageIterator.iterate(any(), any(), any(), any()) } throws RuntimeException("test")

		val images = mSut.images.toList()
		images.assertFirst(isSingle = false)
		assertThat(images.last()).isEqualTo(ImageLoadingResult.error("Ошибка загрузки изображений\ntest"))
	}

	@Test
	fun should_iterate_over_images() = runTest {

		mSut.images.toList().assertFirst()
		coVerifyOnce {
			mImageIterator.iterate(
				any(),
				mImageCursor,
				TREE_URI,
				33.seconds
			)
		}
	}

	private fun newSettings() =
		Settings(
			"imagesDirPath",
			33.seconds,
			scheduleStartSlideShowFlag = false,
			scheduleStopSlideShowFlag = false,
			0,
			0,
			0,
			0,
			startAppOnCharging = false,
			startAppAfterReboot = false
		)

	private suspend fun assertLast(result: ImageLoadingResult) {
		val images = mSut.images.toList()
		images.assertFirst(isSingle = false)
		assertThat(images.last()).isEqualTo(result)
	}

	private fun List<ImageLoadingResult>.assertFirst(isSingle: Boolean = true) {
		if (isSingle)
			assertThat(this).hasSize(1)
		assertThat(first()).isEqualTo(ImageLoadingResult.progress("Загрузка изображений,\nждите..."))
	}
}