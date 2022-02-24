package ru.rizz.slideshow.settings

import kotlin.time.*

data class Settings(
	val imagesDirPath: String,
	val imagesChangeInterval: Duration,
	val scheduleStartSlideShowFlag: Boolean,
	val scheduleStopSlideShowFlag: Boolean,
	val startHour: Int,
	val startMinute: Int,
	val stopHour: Int,
	val stopMinute: Int,
)