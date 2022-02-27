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
import ru.rizz.slideshow.broadcast.*
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
		is Event.PreconditionsViolated -> showToast(ev.text, isLong = false)
		is Event.ErrorOccured -> showToast(ev.text, isLong = true)
		Event.ImagesDirPathChanged -> onImagesDirPathChanged()
		is Event.SettingsSavedWithSchedule -> schedule(ev)
	}

	private fun showToast(text: String, isLong: Boolean) =
		Toast.makeText(requireContext(), text, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

	private fun onImagesDirPathChanged() {
		binding.viewPager.adapter?.notifyItemChanged(0)
	}

	private fun schedule(ev: Event.SettingsSavedWithSchedule) {
		if (ev.scheduleStartSlideShowFlag)
			sendScheduleBroadcast(BroadcastActions.SCHEDULE_START_MAIN_ACTIVITY)
		else if (ev.scheduleStopSlideShowFlag)
			sendScheduleBroadcast(BroadcastActions.SCHEDULE_STOP_MAIN_ACTIVITY)
	}

	private fun sendScheduleBroadcast(action: String) =
		requireContext().sendBroadcast(
			Intent(context, MyBroadcastReceiver::class.java).setAction(action)
		)
}