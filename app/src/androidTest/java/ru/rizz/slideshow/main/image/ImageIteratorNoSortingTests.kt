package ru.rizz.slideshow.main.image

import android.net.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.main.image.iterator.*
import kotlin.time.Duration.Companion.seconds

class ImageIteratorNoSortingTests : SutTestsBase<ImageIteratorNoSorting>() {

	@MockK private lateinit var mImageUriFactory: IImageUriFactory

	@MockK private lateinit var mEmiter: IImageLoadingResultEmiter
	@MockK private lateinit var mCursor: IImageCursor

	private var mImages = mutableListOf<ImageLoadingResult>()

	@Before
	fun setUp() {
		mImages.clear()
	}

	@Test
	fun should_iterate_without_sorting() = runTest {
		every { mCursor.getString(1) }.returnsMany("2", "1")

		iterate()
		assertThat(mImages[0].image!!.title).isEqualTo("2")
		assertThat(mImages[1].image!!.title).isEqualTo("1")
	}

	private suspend fun iterate() {
		every { mCursor.count } returns 3
		every { mCursor.moveToNext() }.returnsMany(true, true, true, false, true)
		every { mCursor.getString(2) }.returnsMany(ImageMimeType.JPEG, "audio/mpeg3", ImageMimeType.PNG)
		coEvery { mEmiter.emit(any()) } answers {
			mImages.add(arg(0))
			if (mImages.size == 2)
				coEvery { mEmiter.emit(any()) } throws RuntimeException("test")
		}

		coAssertThrows<RuntimeException> {
			mSut.iterate(mEmiter, mCursor, Uri.parse("test"), 0.seconds)
		}
	}
}