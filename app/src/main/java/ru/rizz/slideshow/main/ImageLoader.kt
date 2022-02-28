package ru.rizz.slideshow.main

import android.content.*
import android.database.*
import android.net.*
import android.provider.*
import android.provider.DocumentsContract.Document.*
import android.util.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.settings.*
import ru.rizz.slideshow.settings.Settings
import javax.inject.*
import kotlin.time.*

interface IImageLoader {
	val images: Flow<ImageLoadingResult>
}

data class ImageLoadingResult(
	val image: Image?,
	val progress: String = "",
	val error: String = "",
	val dateModifiedMillis: Long = 0,
) {
	companion object {

		fun progress(progress: String) =
			ImageLoadingResult(null, progress = progress)

		fun error(error: String) =
			ImageLoadingResult(null, error = error)
	}
}

data class Image(
	val title: String,
	val uri: Uri,
)

private val TAG = ImageLoader::class.simpleName

class ImageLoader @Inject constructor(
	@ApplicationContext private val mContext: Context,
	private val mSettingsRepository: ISettingsReadonlyRepository
) : IImageLoader {

	private val mSortedImages = arrayListOf<ImageLoadingResult>()
	private var mCurrentPos = 0

	private object MimeType {
		const val JPEG = "image/jpeg"
		const val PNG = "image/png"
	}

	override val images: Flow<ImageLoadingResult> = flow {
		val ss = mSettingsRepository.getSettings()
			?: return@flow
		try {
			emit(ImageLoadingResult.progress("Загрузка изображений,\nждите..."))
			loadImageFiles(ss)
		} catch (e: NoImagesException) {
			Log.w(TAG, "Файлы изображений не найдены")
			emit(ImageLoadingResult.error(e.msg))
		} catch (e: CancellationException) {
			Log.d(TAG, "Слайд-шоу отменено")
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка загрузки изображений", e)
			emit(ImageLoadingResult.error("Ошибка загрузки изображений\n${e.message}"))
		}
	}

	private suspend fun FlowCollector<ImageLoadingResult>.loadImageFiles(ss: Settings) {
		val treeUri = Uri.parse(ss.imagesDirPath)
		val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
		// Выяснилось, что FileProvider игнорирует selectionArgs и selectionOrder.
		// https://androidx.tech/artifacts/core/core/1.1.0-source/androidx/core/content/FileProvider.java.html
		val cursor = mContext.contentResolver.query(
			uri,
			arrayOf(COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_MIME_TYPE, COLUMN_LAST_MODIFIED),
			"$COLUMN_MIME_TYPE=? OR $COLUMN_MIME_TYPE=?",
			arrayOf(MimeType.JPEG, MimeType.PNG),
			"$COLUMN_LAST_MODIFIED DESC"
		) ?: throw NoImagesException("В указанной папке нет файлов изображений")
		cursor.use {
			if (cursor.count == 0)
				throw NoImagesException("В указанной папке нет файлов изображений")
			if (BuildConfigExt.noImageSorting)
				iterateOverImageFilesNoSorting(cursor, treeUri, ss.imagesChangeInterval) else
				iterateOverImageFilesWithSorting(cursor, treeUri, ss.imagesChangeInterval)
		}
	}

	private suspend fun FlowCollector<ImageLoadingResult>.iterateOverImageFilesNoSorting(cursor: Cursor, treeUri: Uri, imagesChangeInterval: Duration) {
		while (true) {
			if (mCurrentPos >= cursor.count)
				mCurrentPos = 0
			cursor.moveToPosition(mCurrentPos - 1)
			while (cursor.moveToNext()) {
				Log.d(TAG, "thread=${Thread.currentThread().name} imagePos=$mCurrentPos total=${cursor.count}")
				val docId = cursor.getString(0)
				val name = cursor.getString(1)
				val mimeType = cursor.getString(2)
				if (mimeType == MimeType.JPEG || mimeType == MimeType.PNG) {
					emit(ImageLoadingResult(Image(name, DocumentsContract.buildDocumentUriUsingTree(treeUri, docId))))
					delay(imagesChangeInterval)
				}
				++mCurrentPos
			}
		}
	}

	private suspend fun FlowCollector<ImageLoadingResult>.iterateOverImageFilesWithSorting(cursor: Cursor, treeUri: Uri, imagesChangeInterval: Duration) {
		cacheSortedImages(cursor, treeUri)
		iterateOverImageFilesWithSortingLoop(imagesChangeInterval)
	}

	private fun cacheSortedImages(cursor: Cursor, treeUri: Uri) {
		mSortedImages.clear()
		mSortedImages.ensureCapacity(cursor.count)
		while (cursor.moveToNext()) {
			val docId = cursor.getString(0)
			val name = cursor.getString(1)
			val mimeType = cursor.getString(2)
			val dateModified = cursor.getLong(3)
			if (mimeType == MimeType.JPEG || mimeType == MimeType.PNG)
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

	private suspend fun FlowCollector<ImageLoadingResult>.iterateOverImageFilesWithSortingLoop(imagesChangeInterval: Duration) {
		while (true) {
			if (mCurrentPos >= mSortedImages.size)
				mCurrentPos = 0
			for (i in mCurrentPos until mSortedImages.size) {
				Log.d(TAG, "thread=${Thread.currentThread().name} imagePos=$mCurrentPos total=${mSortedImages.size}")
				emit(mSortedImages[i])
				delay(imagesChangeInterval)
				++mCurrentPos
			}
		}
	}

	private class NoImagesException(message: String) : RuntimeException(message)
}