package ru.rizz.slideshow.main

import android.view.*
import android.view.animation.*
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
import ru.rizz.slideshow.main.image.*

@AndroidEntryPoint
class MainFragment : FragmentBase<MainVM, Event, FragmentMainBinding>() {

	private lateinit var mFadeIn: Animation

	override val layoutId = R.layout.fragment_main
	override val vm by viewModels<MainVM>()

	override fun onViewCreated() {
		viewLifecycleOwner.lifecycleScope.launch {
			mFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
			vm.onCreate()
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.imageVM.flowOn(Dispatchers.IO).collect {
					bindImageResult(it)
					startAnimation()
				}
			}
		}
	}

	private fun bindImageResult(it: ImageLoadingResult) {
		binding.progress.visibility = if (it.image != null) View.GONE else View.VISIBLE
		if (it.image != null) {
			binding.title.text = it.image.title
			binding.image.setImageURI(it.image.uri)
		} else {
			binding.progress.text = it.progress.ifEmpty { it.error }
		}
	}

	private fun startAnimation() {
		binding.image.startAnimation(mFadeIn)
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.SettingsClick -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
	}
}