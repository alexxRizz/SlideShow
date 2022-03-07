package ru.rizz.slideshow.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.android.components.*
import ru.rizz.slideshow.main.image.*

@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {

	@Binds
	fun imageLoader(value: ImageLoader): IImageLoader
}