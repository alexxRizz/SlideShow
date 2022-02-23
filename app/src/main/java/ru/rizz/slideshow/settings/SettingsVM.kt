package ru.rizz.slideshow.settings

import android.net.*
import android.util.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.common.*
import javax.inject.*
import kotlin.time.Duration.Companion.seconds

private const val TAG = "SettingsVM"

@HiltViewModel
class SettingsVM @Inject constructor(
	private val mSettingsRepository: ISettingsRepository
) : ViewModelBase() {

	sealed class Event : IVmEvent {
		object DirSelectionClick : Event()
		object StartClick : Event()
		data class PreconditionsViolated(val text: String) : Event()
		data class ErrorOccured(val text: String) : Event()
	}

	object ImagesDirPathInfo {
		const val NOT_SET = "не задан"
		const val SET = "задан"
	}

	private val mImagesDirPathLive = MutableLiveData("")

	val imagesDirPathInfoVM = mImagesDirPathLive.map { if (it.isEmpty()) ImagesDirPathInfo.NOT_SET else ImagesDirPathInfo.SET }
	val isImagesDirPathSetVM = imagesDirPathInfoVM.map { it == ImagesDirPathInfo.SET }
	val imagesChangeIntervalVM = MutableLiveData(1)

	fun onCreate() {
		viewModelScope.launch {
			val settings = getSettings()
			mImagesDirPathLive.value = settings?.imagesDirPath ?: ""
			imagesChangeIntervalVM.value = settings?.imagesChangeInterval?.seconds ?: 1
		}
	}

	fun onDirSelectionClick() {
		sendEvent(Event.DirSelectionClick)
	}

	fun onDirSelected(path: Uri) {
		mImagesDirPathLive.value = path.toString()
	}

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

	private suspend fun getSettings() =
		mSettingsRepository.getSettings()

	private suspend fun saveSettings() =
		try {
			mSettingsRepository.setSettings(Settings(
				mImagesDirPathLive.value!!,
				imagesChangeIntervalVM.value!!.seconds)
			)
			true
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка сохранения настроек", e)
			sendEvent(Event.ErrorOccured("Ошибка сохранения настроек\n${e.msg}"))
			false
		}
}