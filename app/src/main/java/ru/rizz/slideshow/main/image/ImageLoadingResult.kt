package ru.rizz.slideshow.main.image

data class ImageLoadingResult(
	val image: Image?,
	val progress: String = "",
	val error: String = "",
	val dateModifiedMillis: Long = 0,
) {
	companion object {

		fun progress(progress: String) =
			ImageLoadingResult(null, progress = progress)

		fun error(error: String) =
			ImageLoadingResult(null, error = error)
	}
}