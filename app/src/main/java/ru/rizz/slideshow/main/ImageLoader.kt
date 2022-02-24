package ru.rizz.slideshow.main

import android.content.*
import android.net.*
import android.util.*
import androidx.documentfile.provider.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.settings.*
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

	override val images: Flow<ImageLoadingResult> = flow {
		val ss = mSettingsRepository.getSettings()
			?: return@flow
		try {
			emit(ImageLoadingResult.progress("Загрузка изображений,\nждите..."))
			val files = loadImageFiles(ss)
			if (files.isNotEmpty())
				iterate(files, ss)
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

	private fun loadImageFiles(ss: Settings): List<DocumentFile> {
		val dir = DocumentFile.fromTreeUri(mContext, Uri.parse(ss.imagesDirPath))
			?: throw NoImagesException("В указанной папке нет файлов изображений")
		val files = dir.listFiles()
			.asSequence()
			.filter { it.isFile && it.canRead() && (it.type == "image/jpeg" || it.type == "image/png") }
			.sortedBy { it.name }
			.toList()
		if (files.isEmpty())
			throw NoImagesException("В указанной папке не найдено ни одного изображения")
		return files
	}

	private suspend fun FlowCollector<ImageLoadingResult>.iterate(files: List<DocumentFile>, ss: Settings) {
		while (true) {
			files.forEach {
				emit(ImageLoadingResult(Image(it.name ?: "", it.uri)))
				delay(ss.imagesChangeInterval)
			}
		}
	}

	private class NoImagesException(message: String) : RuntimeException(message)
}