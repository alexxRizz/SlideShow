package ru.rizz.slideshow

import android.content.*
import android.net.*
import android.os.*
import android.provider.Settings.*
import androidx.activity.result.contract.*
import androidx.appcompat.app.*
import androidx.core.view.*
import androidx.drawerlayout.widget.*
import androidx.navigation.*
import androidx.navigation.fragment.*
import androidx.navigation.ui.*
import dagger.hilt.android.*
import ru.rizz.slideshow.broadcast.*
import ru.rizz.slideshow.databinding.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	companion object {
		var instance: MainActivity? = null; private set
	}

	private lateinit var mBinding: ActivityMainBinding
	private lateinit var mNavController: NavController
	private lateinit var mAppBarConfiguration: AppBarConfiguration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		instance = this
		inflate()
		setupNavigation()
		notifyActivityIsStarted()
		permitStartingActivityFromBackground()
		startChargeForegroundService()
	}

	override fun onDestroy() {
		super.onDestroy()
		instance = null
	}

	private fun inflate() {
		mBinding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(mBinding.root)
	}

	private fun setupNavigation() {
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
		mNavController = navHostFragment.navController
		mAppBarConfiguration = AppBarConfiguration(
			mNavController.graph, //setOf(R.id.mainFragment, R.id.settingsFragment),
			mBinding.drawerLayout
		)
		setupActionBarWithNavController(mNavController, mAppBarConfiguration)
		mBinding.navView.setupWithNavController(mNavController)
	}

	private fun notifyActivityIsStarted() =
		sendBroadcast(
			Intent(this, MyBroadcastReceiver::class.java)
				.setAction(BroadcastActions.MAIN_ACTIVITY_IS_STARTED)
		)

	// https://stackoverflow.com/questions/59419653/cannot-start-activity-background-in-android-10-android-q
	// https://stackoverflow.com/questions/65137688/how-to-open-activity-from-background-services-full-screen-intent-notification-a
	private fun permitStartingActivityFromBackground() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !canDrawOverlays(this)) {
			registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
				.launch(Intent(ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
		}
	}

	private fun startChargeForegroundService() {
		ChargingStatusForegroundService.startService(this)
	}

	override fun onSupportNavigateUp() =
		mNavController.navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()

	override fun onBackPressed() {
		val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
		if (drawerLayout.isDrawerOpen(GravityCompat.START))
			drawerLayout.closeDrawer(GravityCompat.START) else
			super.onBackPressed()
	}
}