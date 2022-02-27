package ru.rizz.slideshow.broadcast

import android.app.*
import android.content.*
import android.os.*
import android.util.*
import androidx.core.app.*
import androidx.core.content.*
import ru.rizz.slideshow.*

private val TAG = ChargingStatusForegroundService::class.simpleName

/** Для получения уведомления о том, что началась зарядка. */
class ChargingStatusForegroundService : Service() {

	companion object {
		private const val CHANNEL_ID = "ChargingBroadcastForegroundService"

		fun startService(context: Context) =
			ContextCompat.startForegroundService(context, Intent(context, ChargingStatusForegroundService::class.java))

		//		fun stopService(context: Context) {
		//			context.stopService(Intent(context, ChargingBroadcastForegroundService::class.java))
		//		}
	}

	private var myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent?) =
			sendBroadcast(
				Intent(context, MyBroadcastReceiver::class.java)
					.setAction(BroadcastActions.START_MAIN_ACTIVITY)
			)
	}


	override fun onCreate() {
		Log.d(TAG, "onCreate()")
		createNotificationChannel()
		val notification = NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("Слайд-шоу")
			.setContentText("Слайд-шоу")
			.setSmallIcon(R.drawable.ic_foreground_service)
			.build()
		startForeground(1, notification)

		registerReceiver(myBroadcastReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
	}


	override fun onStartCommand(resultIntent: Intent?, resultCode: Int, startId: Int): Int {
		Log.d(TAG, "inside onStartCommand() API")
		return startId
	}


	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "inside onDestroy() API")
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			return
		val channel = NotificationChannel(CHANNEL_ID, "Слайд-шоу", NotificationManager.IMPORTANCE_DEFAULT)
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
	}

}