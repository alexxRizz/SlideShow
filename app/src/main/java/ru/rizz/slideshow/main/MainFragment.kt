package ru.rizz.slideshow.main

import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.navigation.fragment.*
import dagger.hilt.android.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rizz.slideshow.R
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.databinding.*
import ru.rizz.slideshow.main.MainVM.*

@AndroidEntryPoint
class MainFragment : FragmentBase<MainVM, Event, FragmentMainBinding>() {

	override val layoutId = R.layout.fragment_main
	override val vm by viewModels<MainVM>()

	override fun onViewCreated() {
		viewLifecycleOwner.lifecycleScope.launch {
			vm.onCreate()
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.imageVM.flowOn(Dispatchers.IO).collect {
					binding.title.text = it.title
					binding.image.setImageURI(it.uri)
				}
			}
		}
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.SettingsClick -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
	}
}