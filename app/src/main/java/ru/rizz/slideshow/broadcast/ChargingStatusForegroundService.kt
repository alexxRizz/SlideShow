package ru.rizz.slideshow.broadcast

import android.app.*
import android.content.*
import android.os.*
import android.util.*
import androidx.core.app.*
import androidx.core.content.*
import dagger.hilt.android.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.R
import ru.rizz.slideshow.settings.*
import javax.inject.*

private val TAG = ChargingStatusForegroundService::class.simpleName

/** Для получения уведомления о том, что началась зарядка. */
@AndroidEntryPoint
class ChargingStatusForegroundService : Service() {

	companion object {
		private const val CHANNEL_ID = "ChargingBroadcastForegroundService"

		fun startService(context: Context) =
			ContextCompat.startForegroundService(context, Intent(context, ChargingStatusForegroundService::class.java))
	}

	private lateinit var mSettingsRepository: ISettingsReadonlyRepository

	@Inject
	fun init(settingsRepository: ISettingsReadonlyRepository) {
		mSettingsRepository = settingsRepository
	}

	private var myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent?) {
			GlobalScope.launch {
				val ss = mSettingsRepository.getSettings()
					?: return@launch
				if (ss.startAppOnCharging)
					sendBroadcast(
						Intent(context, MyBroadcastReceiver::class.java)
							.setAction(BroadcastActions.START_MAIN_ACTIVITY)
					)
			}
		}

	}

	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "onCreate()")
		startService()
		registerReceiver(myBroadcastReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
	}

	private fun startService() {
		createNotificationChannel()
		val notification = NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("Слайд-шоу")
			.setContentText("Слайд-шоу")
			.setSmallIcon(R.drawable.ic_foreground_service)
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setSound(null)
			.build()
		startForeground(1, notification)
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			return
		val channel = NotificationChannel(CHANNEL_ID, "Слайд-шоу", NotificationManager.IMPORTANCE_MIN)
		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
	}

}