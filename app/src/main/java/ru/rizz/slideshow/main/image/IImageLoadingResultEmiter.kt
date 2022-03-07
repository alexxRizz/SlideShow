package ru.rizz.slideshow.main.image

import kotlinx.coroutines.flow.*

interface IImageLoadingResultEmiter {
	suspend fun emit(result: ImageLoadingResult)
}

class ImageLoadingResultEmiter(private val mFlowCollector: FlowCollector<ImageLoadingResult>) : IImageLoadingResultEmiter {

	override suspend fun emit(result: ImageLoadingResult) =
		mFlowCollector.emit(result)
}