package ru.rizz.slideshow

import androidx.appcompat.app.AppCompatDelegate.*
import ru.rizz.slideshow.settings.*
import javax.inject.*

interface IAppThemeUpdater {
	suspend fun run()
}

class AppThemeUpdater @Inject constructor(
	private val mSettingsRepository: ISettingsRepository
) : IAppThemeUpdater {

	override suspend fun run() {
		val ss = mSettingsRepository.getSettings()
		val useDarkTheme = ss?.useDarkTheme ?: false
		setDefaultNightMode(if (useDarkTheme) MODE_NIGHT_YES else MODE_NIGHT_NO)
	}
}