package ru.rizz.slideshow.main.image.iterator

import android.net.*
import ru.rizz.slideshow.main.image.*
import kotlin.time.*

interface IImageIterator {
	suspend fun iterate(emiter: IImageLoadingResultEmiter, cursor: IImageCursor, treeUri: Uri, imagesChangeInterval: Duration)
}