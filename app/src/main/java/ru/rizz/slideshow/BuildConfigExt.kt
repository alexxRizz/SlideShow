package ru.rizz.slideshow

object BuildConfigExt {
	const val noImageSorting = BuildConfig.FLAVOR == "noSorting"
	const val byFileNameSorting = BuildConfig.FLAVOR == "byFileName"
	const val byDateModifiedSorting = BuildConfig.FLAVOR == "byDateModified"
}