package ru.rizz.slideshow.main.image

import android.database.*
import android.net.*
import android.provider.*
import android.util.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.*
import javax.inject.*
import kotlin.time.*

private val TAG = ImageIteratorWithSorting::class.simpleName

class ImageIteratorWithSorting @Inject constructor() : IImageIterator {

	private var mCurrentPos = 0
	private val mSortedImages = arrayListOf<ImageLoadingResult>()

	override suspend fun iterate(emiter: IImageLoadingResultEmiter, cursor: Cursor, treeUri: Uri, imagesChangeInterval: Duration) {
		cacheSortedImages(cursor, treeUri)
		iterateOverImageFilesWithSortingLoop(emiter, imagesChangeInterval)
	}

	private fun cacheSortedImages(cursor: Cursor, treeUri: Uri) {
		mSortedImages.clear()
		mSortedImages.ensureCapacity(cursor.count)
		while (cursor.moveToNext()) {
			val docId = cursor.getString(0)
			val name = cursor.getString(1)
			val mimeType = cursor.getString(2)
			val dateModified = cursor.getLong(3)
			if (mimeType == ImageMimeType.JPEG || mimeType == ImageMimeType.PNG)
				mSortedImages.add(ImageLoadingResult(
					Image(name, DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)),
					dateModifiedMillis = dateModified
				))
		}
		if (BuildConfigExt.byFileNameSorting)
			mSortedImages.sortBy { it.image!!.title }
		else if (BuildConfigExt.byDateModifiedSorting)
			mSortedImages.sortByDescending { it.dateModifiedMillis }
	}

	private suspend fun iterateOverImageFilesWithSortingLoop(emiter: IImageLoadingResultEmiter, imagesChangeInterval: Duration) {
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