package ru.rizz.slideshow.main

import android.content.*
import android.net.*
import androidx.documentfile.provider.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.settings.*
import javax.inject.*

interface IImageLoader {
	val images: Flow<Image>
}

data class Image(
	val title: String,
	val uri: Uri,
)

class ImageLoader @Inject constructor(
	@ApplicationContext private val mContext: Context,
	private val mSettingsRepository: ISettingsReadonlyRepository
) : IImageLoader {

	override val images: Flow<Image> = flow {
		val ss = mSettingsRepository.getSettings()
			?: return@flow
		val dir = DocumentFile.fromTreeUri(mContext, Uri.parse(ss.imagesDirPath))
			?: return@flow
		iterate(dir, ss)
	}

	private suspend fun FlowCollector<Image>.iterate(dir: DocumentFile, ss: Settings) {
		while (true) {
			val files = dir.listFiles().sortedBy { it.name }
			if (files.isEmpty())
				break
			yield()
			files
				.filter { it.isFile && it.canRead() && (it.type == "image/jpeg" || it.type == "image/png") }
				.forEach {
					emit(Image(it.name ?: "", it.uri))
					delay(ss.imagesChangeInterval)
				}
		}
	}
}