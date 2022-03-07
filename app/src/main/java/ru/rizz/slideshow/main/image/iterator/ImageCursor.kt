package ru.rizz.slideshow.main.image.iterator

import android.database.*

interface IImageCursor {
	val count: Int

	fun moveToPosition(pos: Int)
	fun moveToNext(): Boolean

	fun getString(pos: Int): String
	fun getLong(pos: Int): Long
}

class ImageCursor(private val mCursor: Cursor) : IImageCursor {

	override val count: Int get() = mCursor.count

	override fun moveToPosition(pos: Int) {
		mCursor.moveToPosition(pos)
	}

	override fun moveToNext() =
		mCursor.moveToNext()

	override fun getString(pos: Int): String =
		mCursor.getString(pos)

	override fun getLong(pos: Int) =
		mCursor.getLong(pos)
}