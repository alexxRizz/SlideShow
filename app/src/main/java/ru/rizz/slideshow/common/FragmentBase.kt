package ru.rizz.slideshow.common

import android.os.*
import android.view.*
import androidx.databinding.*
import androidx.fragment.app.*
import androidx.navigation.fragment.*
import ru.rizz.slideshow.*

abstract class FragmentBase<
	TModel : ViewModelBase,
	TEvent : IVmEvent,
	TBinding : ViewDataBinding> : Fragment() {

	protected abstract val layoutId: Int
	protected abstract val vm: TModel
	protected abstract fun onViewCreated()
	protected abstract fun onEvent(ev: TEvent)

	protected lateinit var binding: TBinding private set

	final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
		binding.lifecycleOwner = viewLifecycleOwner
		binding.setVariable(BR.vm, vm)
		return binding.root
	}

	final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		vm.eventsFlow.launchAndCollectIn(viewLifecycleOwner) {
			@Suppress("UNCHECKED_CAST")
			onEvent(it as TEvent)
		}
		onViewCreated()
	}

	protected fun <T> androidx.lifecycle.LiveData<T>.observe(observer: androidx.lifecycle.Observer<T>) =
		observe(viewLifecycleOwner, observer)

	protected fun popBackStack() {
		findNavController().popBackStack()
	}
}