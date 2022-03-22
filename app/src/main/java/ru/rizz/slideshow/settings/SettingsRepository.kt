package ru.rizz.slideshow.settings

import android.content.*
import androidx.datastore.core.*
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.common.*
import javax.inject.*
import kotlin.time.Duration.Companion.seconds

interface ISettingsReadonlyRepository {
	suspend fun getSettings(): Settings?
}

interface ISettingsRepository : ISettingsReadonlyRepository {
	suspend fun setSettings(settings: Settings)
}

@Singleton
class SettingsRepository @Inject constructor(
	@ApplicationContext private val mContext: Context
) : ISettingsRepository {

	companion object {
		private val IMAGES_DIR_PATH = stringPreferencesKey("imagesDirPath")
		private val IMAGES_CHANGE_INTERVAL = intPreferencesKey("imagesChangeInterval")
		private val SCHEDULE_START_SLIDE_SHOW_FLAG = booleanPreferencesKey("scheduleStartSlideShowFlag")
		private val SCHEDULE_STOP_SLIDE_SHOW_FLAG = booleanPreferencesKey("scheduleStopSlideShowFlag")
		private val START_HOUR = intPreferencesKey("startHour")
		private val START_MINUTE = intPreferencesKey("startMinute")
		private val STOP_HOUR = intPreferencesKey("stopHour")
		private val STOP_MINUTE = intPreferencesKey("stopMinute")
		private val START_APP_ON_CHARGING = booleanPreferencesKey("startAppOnCharging")
		private val START_APP_AFTER_REBOOT = booleanPreferencesKey("startAppAfterReboot")
		private val USE_DARK_THEME = booleanPreferencesKey("useDarkTheme")
	}

	private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

	override suspend fun getSettings() = withContext(Dispatchers.IO) {
		mContext.dataStore.data.firstOrNull()?.let {
			if (!it.contains(IMAGES_DIR_PATH))
				return@withContext null
			Settings(
				it.require(IMAGES_DIR_PATH),
				it.require(IMAGES_CHANGE_INTERVAL).seconds,
				it.require(SCHEDULE_START_SLIDE_SHOW_FLAG),
				it.require(SCHEDULE_STOP_SLIDE_SHOW_FLAG),
				it.require(START_HOUR),
				it.require(START_MINUTE),
				it.require(STOP_HOUR),
				it.require(STOP_MINUTE),
				it.require(START_APP_ON_CHARGING),
				it.require(START_APP_AFTER_REBOOT),
				it.option(USE_DARK_THEME, false),
			)
		}
	}

	override suspend fun setSettings(settings: Settings) {
		mContext.dataStore.edit {
			it[IMAGES_DIR_PATH] = settings.imagesDirPath
			it[IMAGES_CHANGE_INTERVAL] = settings.imagesChangeInterval.seconds
			it[SCHEDULE_START_SLIDE_SHOW_FLAG] = settings.scheduleStartSlideShowFlag
			it[SCHEDULE_STOP_SLIDE_SHOW_FLAG] = settings.scheduleStopSlideShowFlag
			it[START_HOUR] = settings.startHour
			it[START_MINUTE] = settings.startMinute
			it[STOP_HOUR] = settings.stopHour
			it[STOP_MINUTE] = settings.stopMinute
			it[START_APP_ON_CHARGING] = settings.startAppOnCharging
			it[START_APP_AFTER_REBOOT] = settings.startAppAfterReboot
			it[USE_DARK_THEME] = settings.useDarkTheme
		}
	}

	private fun <T> Preferences.require(key: Preferences.Key<T>) =
		this[key]!!

	private fun <T> Preferences.option(key: Preferences.Key<T>, defValue: T) =
		this[key] ?: defValue
}