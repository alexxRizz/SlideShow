package ru.rizz.slideshow.main.image

import android.net.*
import android.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.main.image.iterator.*
import ru.rizz.slideshow.settings.*
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
	private val mSettingsRepository: ISettingsReadonlyRepository,
	private val mImageIterator: IImageIterator,
	private val mImageCursorFactory: IImageCursorFactory,
) : IImageLoader {

	override val images: Flow<ImageLoadingResult> = flow {
		val ss = mSettingsRepository.getSettings()
			?: return@flow
		try {
			emit(ImageLoadingResult.progress("Загрузка изображений,\nждите..."))
			loadImages(ss, ImageLoadingResultEmiter(this))
		} catch (e: CancellationException) {
			Log.d(TAG, "Слайд-шоу отменено")
		} catch (e: NoImagesException) {
			Log.w(TAG, "Файлы изображений не найдены", e)
			emit(ImageLoadingResult.error(e.msg))
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка загрузки изображений", e)
			emit(ImageLoadingResult.error("Ошибка загрузки изображений\n${e.message}"))
		}
	}

	private suspend fun loadImages(ss: Settings, emiter: IImageLoadingResultEmiter) {
		val treeUri = Uri.parse(ss.imagesDirPath)
		val imageCursor = mImageCursorFactory.new(treeUri)
			?: throw NoImagesException("В указанной папке нет файлов изображений")
		imageCursor.use {
			if (imageCursor.count == 0)
				throw NoImagesException("В указанной папке нет файлов изображений")
			mImageIterator.iterate(emiter, it, treeUri, ss.imagesChangeInterval)
		}
	}

	private class NoImagesException(message: String) : RuntimeException(message)
}