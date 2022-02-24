package ru.rizz.slideshow.autoload

import android.content.*
import android.util.*
import ru.rizz.slideshow.*

private val TAG = BootReceiver::class.simpleName

class BootReceiver : BroadcastReceiver() {

	private var mWasMainActivityStarted = false

	override fun onReceive(context: Context, anIntent: Intent?) {
		if (anIntent == null)
			Log.d(TAG, "onReceive()") else
			Log.d(TAG, "onReceive(${anIntent.action})")
		if (mWasMainActivityStarted) {
			Log.d(TAG, "Activity was already started")
			return
		}
		if (anIntent?.action == Intent.ACTION_BOOT_COMPLETED || anIntent?.action == "android.intent.action.QUICKBOOT_POWERON") {
			mWasMainActivityStarted = true
			Log.d(TAG, "Starting main activity...")
			val intent = Intent(context, MainActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(intent)
		}
	}
}