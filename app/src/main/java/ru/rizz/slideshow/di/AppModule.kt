package ru.rizz.slideshow.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*
import ru.rizz.slideshow.settings.*

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

	@Binds
	fun settingsReadonlyRepository(value: SettingsRepository): ISettingsReadonlyRepository

	@Binds
	fun settingsRepository(value: SettingsRepository): ISettingsRepository
}