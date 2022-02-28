package ru.rizz.slideshow.broadcast

import android.app.*
import android.content.*
import android.os.*
import android.util.*
import dagger.hilt.android.*
import kotlinx.coroutines.*
import ru.rizz.slideshow.*
import ru.rizz.slideshow.settings.*
import java.util.*
import javax.inject.*

private val TAG = MyBroadcastReceiver::class.simpleName

@AndroidEntryPoint
class MyBroadcastReceiver : BroadcastReceiver() {

	companion object {
		const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000 // 24h * 60m * 60s * 1000ms
	}

	private lateinit var mSettingsRepository: ISettingsReadonlyRepository

	@Inject
	fun init(settingsRepository: ISettingsReadonlyRepository) {
		mSettingsRepository = settingsRepository
	}

	private var mWasMainActivityStarted = false

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent == null) {
			Log.d(TAG, "onReceive()")
			return
		}
		Log.d(TAG, "onReceive(${intent.action})")
		when (intent.action) {
			Intent.ACTION_BOOT_COMPLETED,
			BroadcastActions.QUICKBOOT_POWERON -> onBootCompleted(context)

			BroadcastActions.START_MAIN_ACTIVITY -> startActivityIfNeeded(context)
			BroadcastActions.STOP_MAIN_ACTIVITY -> stopActivity()

			BroadcastActions.UPDATE_SCHEDULE -> launchScheduleUpdate(context)

			BroadcastActions.MAIN_ACTIVITY_IS_STARTED -> onMainActivityStarted(context)
		}
	}

	private fun onBootCompleted(context: Context) {
		launchScheduleUpdate(context)
		launchMainActivityIfNeeded(context)
	}

	private fun launchScheduleUpdate(context: Context) {
		GlobalScope.launch {
			updateSchedule(context)
		}
	}

	private fun launchMainActivityIfNeeded(context: Context) {
		GlobalScope.launch {
			mSettingsRepository.getSettings()?.let {
				if (it.startAppAfterReboot)
					startActivityIfNeeded(context)
			}
		}
	}

	private fun onMainActivityStarted(context: Context) {
		mWasMainActivityStarted = true
		launchScheduleUpdate(context)
	}

	private fun startActivityIfNeeded(context: Context) {
		if (mWasMainActivityStarted) {
			Log.d(TAG, "MainActivity was already started")
			return
		}
		mWasMainActivityStarted = true
		Log.d(TAG, "Starting main activity...")
		val intent = Intent(context, MainActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(intent)
	}

	private suspend fun updateSchedule(context: Context) {
		val ss = mSettingsRepository.getSettings()
			?: return
		setAlarm(context, ss.startHour, ss.startMinute, ss.scheduleStartSlideShowFlag, 1, BroadcastActions.START_MAIN_ACTIVITY)
		setAlarm(context, ss.stopHour, ss.stopMinute, ss.scheduleStopSlideShowFlag, 2, BroadcastActions.STOP_MAIN_ACTIVITY)
	}

	private fun setAlarm(context: Context, hour: Int, minute: Int, shouldSchedule: Boolean, requestCode: Int, action: String) {
		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val triggerTime = getTriggerTime(hour, minute)
		val flags =
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else
				PendingIntent.FLAG_UPDATE_CURRENT
		val intent = Intent(context, MyBroadcastReceiver::class.java).apply { this.action = action }
		val triggerIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
		if (shouldSchedule) {
			alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, null), triggerIntent)
			Log.d(TAG, "Alarm is set to ${hour}:${minute} | $action")
		} else {
			alarmManager.cancel(triggerIntent)
			Log.d(TAG, "Alarm canceled ${hour}:${minute} | $action")
		}
	}

	private fun getTriggerTime(hour: Int, minute: Int): Long {
		val todayTimeMillis = getTodayTimeMillis(hour, minute)
		val currentTimeMillis = System.currentTimeMillis()
		val delta = todayTimeMillis - currentTimeMillis
		return if (delta > 5_000) todayTimeMillis else currentTimeMillis + ONE_DAY_IN_MILLIS
	}

	private fun getTodayTimeMillis(hour: Int, minute: Int): Long {
		val calendar = Calendar.getInstance()
		calendar[Calendar.HOUR_OF_DAY] = hour
		calendar[Calendar.MINUTE] = minute
		calendar[Calendar.SECOND] = 0
		return calendar.timeInMillis
	}

	private fun stopActivity() {
		MainActivity.instance?.finishAndRemoveTask()
	}
}