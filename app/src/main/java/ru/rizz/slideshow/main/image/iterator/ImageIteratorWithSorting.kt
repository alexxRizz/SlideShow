package ru.rizz.slideshow.main.image.iterator

import android.net.*
import android.util.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.main.image.*
import javax.inject.*
import kotlin.time.*

private val TAG = ImageIteratorWithSorting::class.simpleName

class ImageIteratorWithSorting @Inject constructor(
	private val mImageUriFactory: IImageUriFactory,
) : IImageIterator {

	private var mCurrentPos = 0
	private val mSortedImages = arrayListOf<ImageLoadingResult>()

	override suspend fun iterate(emiter: IImageLoadingResultEmiter, cursor: IImageCursor, treeUri: Uri, imagesChangeInterval: Duration) {
		cacheSortedImages(cursor, treeUri)
		iterate(emiter, imagesChangeInterval)
	}

	private fun cacheSortedImages(cursor: IImageCursor, treeUri: Uri) {
		mSortedImages.clear()
		mSortedImages.ensureCapacity(cursor.count)
		while (cursor.moveToNext()) {
			val docId = cursor.getString(0)
			val name = cursor.getString(1)
			val mimeType = cursor.getString(2)
			val dateModified = cursor.getLong(3)
			if (mimeType == ImageMimeType.JPEG || mimeType == ImageMimeType.PNG)
				mSortedImages.add(ImageLoadingResult(
					Image(name, mImageUriFactory.new(treeUri, docId)),
					dateModifiedMillis = dateModified
				))
		}
		if (BuildConfigExt.byFileNameSorting)
			mSortedImages.sortBy { it.image!!.title }
		else if (BuildConfigExt.byDateModifiedSorting)
			mSortedImages.sortByDescending { it.dateModifiedMillis }
	}

	private suspend fun iterate(emiter: IImageLoadingResultEmiter, imagesChangeInterval: Duration) {
		while (true) {
			if (mCurrentPos >= mSortedImages.size)
				mCurrentPos = 0
			for (i in mCurrentPos until mSortedImages.size) {
				Log.d(TAG, "thread=${Thread.currentThread().name} imagePos=$mCurrentPos total=${mSortedImages.size}")
				emiter.emit(mSortedImages[i])
				delay(imagesChangeInterval)
				++mCurrentPos
			}
		}
	}
}