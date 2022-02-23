package ru.rizz.slideshow

import android.os.*
import androidx.appcompat.app.*
import androidx.core.view.*
import androidx.drawerlayout.widget.*
import androidx.navigation.*
import androidx.navigation.fragment.*
import androidx.navigation.ui.*
import dagger.hilt.android.*
import ru.rizz.slideshow.databinding.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	private lateinit var mBinding: ActivityMainBinding
	private lateinit var mNavController: NavController
	private lateinit var mAppBarConfiguration: AppBarConfiguration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		inflate()
		setupNavigation()
	}

	private fun inflate() {
		mBinding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(mBinding.root)
	}

	private fun setupNavigation() {
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
		mNavController = navHostFragment.navController
		mAppBarConfiguration = AppBarConfiguration(
			setOf(R.id.mainFragment, R.id.settingsFragment),
			mBinding.drawerLayout
		)
		setupActionBarWithNavController(mNavController, mAppBarConfiguration)
		mBinding.navView.setupWithNavController(mNavController)
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