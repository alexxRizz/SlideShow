package ru.rizz.slideshow.settings

import android.content.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.*
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

	private val mDirSelection = registerForActivityResult(OpenDocumentTree()) {
		if (it != null) {
			requireContext().contentResolver.releasePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
			vm.onDirSelected(it)
		}
	}

	override fun onViewCreated() {
		vm.onCreate()
	}

	override fun onEvent(ev: Event) = when (ev) {
		Event.DirSelectionClick -> mDirSelection.launch(null)
		Event.StartClick -> popBackStack()
		is Event.PreconditionsViolated -> Toast.makeText(context, ev.text, Toast.LENGTH_SHORT).show()
		is Event.ErrorOccured -> Toast.makeText(context, ev.text, Toast.LENGTH_LONG).show()
	}
}