package ru.rizz.slideshow.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.android.components.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.main.image.iterator.*

@Module
@InstallIn(ViewModelComponent::class)
class ImageModuleProvider {

	@Provides
	fun imageIterator(noSorting: ImageIteratorNoSorting, withSorting: ImageIteratorWithSorting) =
		if (BuildConfigExt.noImageSorting)
			noSorting else
			withSorting
}