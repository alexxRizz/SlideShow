package ru.rizz.slideshow.settings

import kotlin.time.*

data class Settings(
	val imagesDirPath: String,
	val imagesChangeInterval: Duration
)