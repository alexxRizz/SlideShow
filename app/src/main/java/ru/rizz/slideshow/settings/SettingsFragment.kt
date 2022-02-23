package ru.rizz.slideshow.settings

import androidx.fragment.app.*
import dagger.hilt.android.*
import ru.rizz.slideshow.R
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.databinding.*
import ru.rizz.slideshow.settings.SettingsVM.*

@AndroidEntryPoint
class SettingsFragment : FragmentBase<SettingsVM, Event, FragmentSettingsBinding>() {

	override val layoutId = R.layout.fragment_settings
	override val vm by viewModels<SettingsVM>()

	override fun onViewCreated() {
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.Zzz -> TODO()
	}
}