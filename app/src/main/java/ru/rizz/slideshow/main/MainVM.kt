package ru.rizz.slideshow.main

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.settings.*
import javax.inject.*

@HiltViewModel
class MainVM @Inject constructor(
	private val mSettingsRepository: ISettingsReadonlyRepository
) : ViewModelBase() {

	sealed class Event : IVmEvent {
		object SettingsClick : Event()
	}

	val hasSettingsVM = MutableLiveData(false)

	fun onCreate() {
		viewModelScope.launch {
			val settings = mSettingsRepository.getSettings()
			hasSettingsVM.value = settings != null
		}
	}

	fun onSettingsPromptClick() =
		sendEvent(Event.SettingsClick)

	// https://stackoverflow.com/questions/47941357/how-to-access-files-in-a-directory-given-a-content-uri
	// DocumentFile.fromTreeUri()
}