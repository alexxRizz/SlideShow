package ru.rizz.slideshow.main.image

import android.content.*
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

data class Image(
	val title: String,
	val uri: Uri,
)

private val TAG = ImageLoader::class.simpleName

class ImageLoader @Inject constructor(
	@ApplicationContext private val mContext: Context,
	private val mSettingsRepository: ISettingsReadonlyRepository,
	private val mImageIterator: IImageIterator,
) : IImageLoader {

	override val images: Flow<ImageLoadingResult> = flow {
		val ss = mSettingsRepository.getSettings()
			?: return@flow
		try {
			emit(ImageLoadingResult.progress("Загрузка изображений,\nждите..."))
			loadImageFiles(ss, ImageLoadingResultEmiter(this))
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

	private suspend fun loadImageFiles(ss: Settings, emiter: IImageLoadingResultEmiter) {
		val treeUri = Uri.parse(ss.imagesDirPath)
		val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
		// Выяснилось, что FileProvider игнорирует selectionArgs и selectionOrder.
		// https://androidx.tech/artifacts/core/core/1.1.0-source/androidx/core/content/FileProvider.java.html
		val cursor = mContext.contentResolver.query(
			uri,
			arrayOf(COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_MIME_TYPE, COLUMN_LAST_MODIFIED),
			"$COLUMN_MIME_TYPE=? OR $COLUMN_MIME_TYPE=?",
			arrayOf(ImageMimeType.JPEG, ImageMimeType.PNG),
			"$COLUMN_LAST_MODIFIED DESC"
		) ?: throw NoImagesException("В указанной папке нет файлов изображений")
		cursor.use {
			if (cursor.count == 0)
				throw NoImagesException("В указанной папке нет файлов изображений")
			mImageIterator.iterate(emiter, cursor, treeUri, ss.imagesChangeInterval)
		}
	}

	private class NoImagesException(message: String) : RuntimeException(message)
}