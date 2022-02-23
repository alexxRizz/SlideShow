package ru.rizz.slideshow.settings

import android.content.*
import androidx.datastore.core.*
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.flow.*
import javax.inject.*
import kotlin.time.*
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
		private val PICTURE_DIR_PATH = stringPreferencesKey("pictureDirPath")
		private val PICTURE_CHANGE_INTERVAL = intPreferencesKey("pictureChangeInterval")
	}

	private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

	override suspend fun getSettings() =
		mContext.dataStore.data.firstOrNull()?.let {
			if (it[PICTURE_DIR_PATH] == null)
				return null
			Settings(
				it[PICTURE_DIR_PATH] ?: "",
				it[PICTURE_CHANGE_INTERVAL]?.seconds ?: 1.seconds,
			)
		}

	override suspend fun setSettings(settings: Settings) {
		mContext.dataStore.edit {
			it[PICTURE_DIR_PATH] = settings.pictureDirPath
			it[PICTURE_CHANGE_INTERVAL] = settings.pictureChangeInterval.toInt(DurationUnit.SECONDS)
		}
	}
}