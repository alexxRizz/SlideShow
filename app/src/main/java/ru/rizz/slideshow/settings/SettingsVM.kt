package ru.rizz.slideshow.settings

import android.net.*
import android.util.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.common.*
import javax.inject.*
import kotlin.time.Duration.Companion.seconds

private val TAG = SettingsVM::class.simpleName

@HiltViewModel
class SettingsVM @Inject constructor(
	private val mSettingsRepository: ISettingsRepository
) : ViewModelBase() {

	sealed class Event : IVmEvent {
		object DirSelectionClick : Event()
		object StartClick : Event()
		data class PreconditionsViolated(val text: String) : Event()
		data class ErrorOccured(val text: String) : Event()
		object ImagesDirPathChanged : Event()
	}

	object ImagesDirPathInfo {
		const val NOT_SET = "не задан"
		const val SET = "задан"
	}

	object DefaultSettings {
		const val IMAGES_DIR_PATH = ""
		const val IMAGES_CHANGE_INTERVAL = 3
		const val SCHEDULE_SLIDE_SHOW_FLAGS = false
		const val START_HOUR = 0
		const val START_MINUTE = 0
		const val STOP_HOUR = 0
		const val STOP_MINUTE = 0
	}

	private val mImagesDirPathLive = MutableLiveData("")

	val imagesDirPathInfoVM = MutableLiveData("")
	val isImagesDirPathSetVM = MutableLiveData(false)
	val imagesChangeIntervalVM = MutableLiveData(1)
	val scheduleStartSlideShowFlagVM = MutableLiveData(false)
	val scheduleStopSlideShowFlagVM = MutableLiveData(false)
	val startHourVM = MutableLiveData(0)
	val startMinuteVM = MutableLiveData(0)
	val stopHourVM = MutableLiveData(0)
	val stopMinuteVM = MutableLiveData(0)

	fun onCreate() {
		viewModelScope.launch {
			loadSettings()
		}
	}

	fun onDirSelectionClick() {
		sendEvent(Event.DirSelectionClick)
	}

	fun onDirSelected(path: Uri) =
		bindImagesDirPath(path.toString())

	fun onStartClick() {
		if (mImagesDirPathLive.value.isNullOrEmpty()) {
			sendEvent(Event.PreconditionsViolated("Выберите папку - источник изображений"))
			return
		}
		viewModelScope.launch {
			if (saveSettings())
				sendEvent(Event.StartClick)
		}
	}

	private suspend fun loadSettings() {
		val settings = mSettingsRepository.getSettings()
		val dirPath = settings?.imagesDirPath ?: DefaultSettings.IMAGES_DIR_PATH
		bindImagesDirPath(dirPath)
		imagesChangeIntervalVM.value = settings?.imagesChangeInterval?.seconds ?: DefaultSettings.IMAGES_CHANGE_INTERVAL
		scheduleStartSlideShowFlagVM.value = settings?.scheduleStartSlideShowFlag ?: DefaultSettings.SCHEDULE_SLIDE_SHOW_FLAGS
		scheduleStopSlideShowFlagVM.value = settings?.scheduleStopSlideShowFlag ?: DefaultSettings.SCHEDULE_SLIDE_SHOW_FLAGS
		startHourVM.value = settings?.startHour ?: DefaultSettings.START_HOUR
		startMinuteVM.value = settings?.startMinute ?: DefaultSettings.START_MINUTE
		stopHourVM.value = settings?.stopHour ?: DefaultSettings.STOP_HOUR
		stopMinuteVM.value = settings?.stopMinute ?: DefaultSettings.STOP_MINUTE
	}

	private fun bindImagesDirPath(path: String) {
		mImagesDirPathLive.value = path
		imagesDirPathInfoVM.value = if (path.isEmpty()) ImagesDirPathInfo.NOT_SET else ImagesDirPathInfo.SET
		isImagesDirPathSetVM.value = path.isNotEmpty()
		sendEvent(Event.ImagesDirPathChanged)
	}

	private suspend fun saveSettings() =
		try {
			mSettingsRepository.setSettings(Settings(
				mImagesDirPathLive.require,
				imagesChangeIntervalVM.require.seconds,
				scheduleStartSlideShowFlagVM.require,
				scheduleStopSlideShowFlagVM.require,
				startHourVM.require,
				startMinuteVM.require,
				stopHourVM.require,
				stopMinuteVM.require,
			))
			true
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка сохранения настроек", e)
			sendEvent(Event.ErrorOccured("Ошибка сохранения настроек\n${e.msg}"))
			false
		}
}