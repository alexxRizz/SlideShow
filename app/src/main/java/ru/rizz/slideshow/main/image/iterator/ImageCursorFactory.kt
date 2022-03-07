package ru.rizz.slideshow.main.image.iterator

import android.content.*
import android.net.*
import android.provider.*
import dagger.hilt.android.qualifiers.*
import ru.rizz.slideshow.main.image.*
import javax.inject.*

interface IImageCursorFactory {
	fun new(treeUri: Uri): IImageCursor?
}

class ImageCursorFactory @Inject constructor(
	@ApplicationContext private val mContext: Context,
) : IImageCursorFactory {

	override fun new(treeUri: Uri): IImageCursor? {
		val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
		// Выяснилось, что FileProvider игнорирует selectionArgs и selectionOrder.
		// https://androidx.tech/artifacts/core/core/1.1.0-source/androidx/core/content/FileProvider.java.html
		val cursor = mContext.contentResolver.query(
			uri,
			arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_LAST_MODIFIED),
			"${DocumentsContract.Document.COLUMN_MIME_TYPE}=? OR ${DocumentsContract.Document.COLUMN_MIME_TYPE}=?",
			arrayOf(ImageMimeType.JPEG, ImageMimeType.PNG),
			"${DocumentsContract.Document.COLUMN_LAST_MODIFIED} DESC"
		)
		return if (cursor == null) null else ImageCursor(cursor)
	}
}