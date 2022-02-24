package ru.rizz.slideshow.settings

import android.view.*
import androidx.databinding.*
import androidx.recyclerview.widget.*
import ru.rizz.slideshow.*

class SettingsTabAdapter(
	private val mSettingsVM: SettingsVM
) : RecyclerView.Adapter<SettingsTabAdapter.MyViewHolder>() {

	class MyViewHolder(root: View) : RecyclerView.ViewHolder(root)

	private object ViewType {
		const val MAIN_SETTINGS = 0
		const val SCHEDULE_SETTINGS = 1
	}

	private class ItemView(
		val viewType: Int,
		val layoutId: Int,
		val tabTitle: String,
	)

	companion object {
		private val ItemViews = arrayOf(
			ItemView(ViewType.MAIN_SETTINGS, R.layout.view_main_settings, "Основные"),
			ItemView(ViewType.SCHEDULE_SETTINGS, R.layout.view_schedule_settings, "По расписанию"),
		)

		fun getTabTitle(pos: Int) =
			ItemViews[pos].tabTitle
	}

	private val mBindings = arrayOfNulls<ViewDataBinding>(2)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
		val layoutId = ItemViews[viewType].layoutId
		val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
		mBindings[viewType] = binding
		return MyViewHolder(binding.root)
	}

	override fun getItemViewType(position: Int) =
		ItemViews[position].viewType

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		mBindings[position]?.apply {
			setVariable(BR.vm, mSettingsVM)
			executePendingBindings()
		}
	}

	override fun getItemCount(): Int = 2
}