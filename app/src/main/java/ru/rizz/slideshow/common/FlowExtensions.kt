package ru.rizz.slideshow.common

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// Метод придуман на основе статей:
// https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
// https://medium.com/androiddevelopers/repeatonlifecycle-api-design-story-8670d1a7d333
//
// PS: Тут более свежие рекомендации, но не значит, что более правильные,
// о чем и говорит автор в конце статьи:
// https://proandroiddev.com/sending-view-model-events-to-the-ui-eef76bdd632c
fun <T> Flow<T>.launchAndCollectIn(owner: LifecycleOwner, onCollect: (ev: T) -> Unit) =
	owner.lifecycleScope.launch {
		owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			collect {
				onCollect(it)
			}
		}
	}