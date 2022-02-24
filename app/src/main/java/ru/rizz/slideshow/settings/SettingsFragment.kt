package ru.rizz.slideshow.settings

import android.content.*
import android.util.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.fragment.app.*
import com.google.android.material.tabs.*
import dagger.hilt.android.*
import ru.rizz.slideshow.R
import ru.rizz.slideshow.common.*
import ru.rizz.slideshow.databinding.*
import ru.rizz.slideshow.settings.SettingsVM.*

private val TAG = SettingsFragment::class.simpleName

@AndroidEntryPoint
class SettingsFragment : FragmentBase<SettingsVM, Event, FragmentSettingsBinding>() {

	override val layoutId = R.layout.fragment_settings
	override val vm by viewModels<SettingsVM>()

	private val mDirSelection = registerForActivityResult(OpenDocumentTree()) {
		if (it == null) {
			Log.w(TAG, "registerForActivityResult(): null uri received")
			return@registerForActivityResult
		}
		try {
			requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
		} catch (e: Exception) {
			Log.e(TAG, "Ошибка takePersistableUriPermission()", e)
		}
		vm.onDirSelected(it)
	}

	override fun onViewCreated() {
		vm.onCreate()
		binding.viewPager.adapter = SettingsTabAdapter(vm)
		TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos: Int ->
			tab.text = SettingsTabAdapter.getTabTitle(pos)
		}.attach()
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.DirSelectionClick -> mDirSelection.launch(null)
		Event.StartClick -> popBackStack()
		is Event.PreconditionsViolated -> Toast.makeText(context, ev.text, Toast.LENGTH_SHORT).show()
		is Event.ErrorOccured -> Toast.makeText(context, ev.text, Toast.LENGTH_LONG).show()
		Event.ImagesDirPathChanged -> onImagesDirPathChanged()
	}

	private fun onImagesDirPathChanged() {
		binding.viewPager.adapter?.notifyItemChanged(0)
	}
}