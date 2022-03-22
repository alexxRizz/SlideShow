package ru.rizz.slideshow

import android.app.*
import dagger.hilt.android.*
import kotlinx.coroutines.*
import javax.inject.*

@HiltAndroidApp
class SlideShowApp : Application() {

	private val mApplicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

	private lateinit var mAppThemeUpdater: IAppThemeUpdater

	@Inject
	fun inject(appThemeUpdater: IAppThemeUpdater) {
		mAppThemeUpdater = appThemeUpdater
	}

	override fun onCreate() {
		super.onCreate()
		mApplicationScope.launch {
			mAppThemeUpdater.run()
		}
	}
}