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
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.settings.*
import ru.rizz.slideshow.settings.Settings
import javax.inject.*

interface IImageLoader {
	val images: Flow<ImageLoadingResult>
}

data class ImageLoadingResult(
	val image: Image?,
	val progress: String = "",
	val error: String = ""
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
			Log.d(TAG, "Загрузка изображений отменена")
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
			iterateOverImageFiles(cursor, ss, treeUri)
		}
	}

	private suspend fun FlowCollector<ImageLoadingResult>.iterateOverImageFiles(cursor: Cursor, ss: Settings, treeUri: Uri) {
		while (true) {
			cursor.moveToPosition(-1)
			while (cursor.moveToNext()) {
				val docId = cursor.getString(0)
				val name = cursor.getString(1)
				val mimeType = cursor.getString(2)
				if (mimeType == MimeType.JPEG || mimeType == MimeType.PNG) {
					emit(ImageLoadingResult(Image(name, DocumentsContract.buildDocumentUriUsingTree(treeUri, docId))))
					delay(ss.imagesChangeInterval)
				}
			}
		}
	}

	private class NoImagesException(message: String) : RuntimeException(message)
}