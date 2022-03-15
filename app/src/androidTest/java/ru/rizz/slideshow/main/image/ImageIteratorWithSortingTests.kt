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

class ImageIteratorWithSortingTests : SutTestsBase<ImageIteratorWithSorting>() {

	@MockK private lateinit var mImageUriFactory: IImageUriFactory
	@MockK private lateinit var mImageSortingTypeProvider: IImageSortingTypeProvider

	@MockK private lateinit var mEmiter: IImageLoadingResultEmiter
	@MockK private lateinit var mCursor: IImageCursor

	private var mImages = mutableListOf<ImageLoadingResult>()

	@Before
	fun setUp() {
		mImages.clear()
	}

	@Test
	fun should_iterate_sorted_by_file_name() = runTest {
		every { mImageSortingTypeProvider.type } returns ImageSortingType.BY_FILE_NAME
		every { mCursor.getString(1) }.returnsMany("2", "1")

		iterate()
		assertThat(mImages[0].image!!.title).isEqualTo("1")
		assertThat(mImages[1].image!!.title).isEqualTo("2")
	}

	@Test
	fun should_iterate_sorted_by_date_modified() = runTest {
		every { mImageSortingTypeProvider.type } returns ImageSortingType.BY_DATE_MODIFIED
		every { mCursor.getLong(3) }.returnsMany(10L, 20L)

		iterate()
		assertThat(mImages[0].dateModifiedMillis).isEqualTo(20L)
		assertThat(mImages[1].dateModifiedMillis).isEqualTo(10L)
	}

	private suspend fun iterate() {
		every { mCursor.count } returns 3
		every { mCursor.moveToNext() }.returnsMany(true, true, true, false)
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