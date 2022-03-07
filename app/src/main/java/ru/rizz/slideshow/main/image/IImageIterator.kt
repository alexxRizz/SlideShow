package ru.rizz.slideshow.main.image

import android.database.*
import android.net.*
import kotlin.time.*

interface IImageIterator {
	suspend fun iterate(emiter: IImageLoadingResultEmiter, cursor: Cursor, treeUri: Uri, imagesChangeInterval: Duration)
}