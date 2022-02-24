package ru.rizz.slideshow.main

import android.util.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.settings.*
import javax.inject.*

private val TAG = MainVM::class.simpleName

@HiltViewModel
class MainVM @Inject constructor(
	imageLoader: IImageLoader,
	private val mSettingsRepository: ISettingsReadonlyRepository,
) : ViewModelBase() {

	sealed class Event : IVmEvent {
		object SettingsClick : Event()
	}

	private var mSettings: Settings? = null

	val hasSettingsVM = MutableLiveData(true)
	val imageVM = imageLoader.images

	suspend fun onCreate() {
		try {
			mSettings = mSettingsRepository.getSettings()
			hasSettingsVM.value = mSettings != null
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка запуска слайд-шоу", e)
		}
	}

	fun onSettingsPromptClick() =
		sendEvent(Event.SettingsClick)
}