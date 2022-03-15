package ru.rizz.slideshow.main.image.iterator

import ru.rizz.slideshow.*
import javax.inject.*

enum class ImageSortingType {
	BY_FILE_NAME,
	BY_DATE_MODIFIED
}

interface IImageSortingTypeProvider {
	val type: ImageSortingType
}

class ImageSortingTypeProvider @Inject constructor() : IImageSortingTypeProvider {
	override val type: ImageSortingType
		get() =
			when {
				BuildConfigExt.byFileNameSorting -> ImageSortingType.BY_FILE_NAME
				BuildConfigExt.byDateModifiedSorting -> ImageSortingType.BY_DATE_MODIFIED
				else -> throw NotImplementedError("Не поддерживаемый тип сортировки изображений: ${BuildConfig.FLAVOR}")
			}
}