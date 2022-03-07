package ru.rizz.slideshow.main.image.iterator

import android.net.*
import android.provider.*
import javax.inject.*

interface IImageUriFactory {
	fun new(treeUri: Uri, docId: String): Uri
}

class ImageUriFactory @Inject constructor() : IImageUriFactory {

	override fun new(treeUri: Uri, docId: String): Uri =
		DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
}