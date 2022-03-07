package ru.rizz.slideshow.main.image.iterator

import android.net.*
import android.provider.*
import android.util.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.main.image.*
import javax.inject.*
import kotlin.time.*

private val TAG = ImageIteratorNoSorting::class.simpleName

class ImageIteratorNoSorting @Inject constructor() : IImageIterator {

	private var mCurrentPos = 0

	override suspend fun iterate(emiter: IImageLoadingResultEmiter, cursor: IImageCursor, treeUri: Uri, imagesChangeInterval: Duration) {
		while (true) {
			if (mCurrentPos >= cursor.count)
				mCurrentPos = 0
			cursor.moveToPosition(mCurrentPos - 1)
			while (cursor.moveToNext()) {
				Log.d(TAG, "thread=${Thread.currentThread().name} imagePos=$mCurrentPos total=${cursor.count}")
				val docId = cursor.getString(0)
				val name = cursor.getString(1)
				val mimeType = cursor.getString(2)
				if (mimeType == ImageMimeType.JPEG || mimeType == ImageMimeType.PNG) {
					emiter.emit(ImageLoadingResult(Image(name, DocumentsContract.buildDocumentUriUsingTree(treeUri, docId))))
					delay(imagesChangeInterval)
				}
				++mCurrentPos
			}
		}
	}
}