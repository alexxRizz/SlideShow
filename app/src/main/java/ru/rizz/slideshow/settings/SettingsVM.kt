package ru.rizz.slideshow.settings

import dagger.hilt.android.lifecycle.*
import ru.rizz.slideshow.common.*
import javax.inject.*

@HiltViewModel
class SettingsVM @Inject constructor() : ViewModelBase() {

	sealed class Event : IVmEvent {
		object Zzz : Event()
	}
}