package ru.rizz.slideshow.settings

import kotlin.time.*

data class Settings(
	val pictureDirPath: String,
	val pictureChangeInterval: Duration
)