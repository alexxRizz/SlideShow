package ru.rizz.slideshow.main

import androidx.fragment.app.*
import androidx.navigation.fragment.*
import dagger.hilt.android.*
import ru.rizz.slideshow.R
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.databinding.*
import ru.rizz.slideshow.main.MainVM.*

@AndroidEntryPoint
class MainFragment : FragmentBase<MainVM, Event, FragmentMainBinding>() {

	override val layoutId = R.layout.fragment_main
	override val vm by viewModels<MainVM>()

	override fun onViewCreated() {
		vm.onCreate()
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.SettingsClick -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
	}
}