package com.example.butterflydetector

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.butterflydetector.databinding.ActivityMainBinding
import com.example.butterflydetector.ui.home.HomeFragment
import com.example.butterflydetector.data.ButterflyDatabase
import com.example.butterflydetector.data.ButterflyEntity
import com.example.butterflydetector.utils.ColorModeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.edit

class MainActivity : AppCompatActivity(), HomeFragment.CameraButtonController {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private var photoBtn: LinearLayout? = null
    private var cameraBtn: LinearLayout? = null
    private var transectsBtn: LinearLayout? = null
    private var cameraButtonIcon: ImageView? = null

    private var pendingCaptureAfterNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Apply colors based on current mode
        applyColorMode()

        initializeDatabase()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_camera,
                R.id.nav_photoselection,
                R.id.nav_speciescatalog,
                R.id.nav_transects,
                R.id.nav_transectwalks,
                R.id.nav_tutorial
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        setupBottomNavigation()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomSelection(destination.id)

            val homeFragment = getCurrentHomeFragment()

            if (destination.id != R.id.nav_camera) {
                homeFragment?.stopPhotoCapture()
            }

            if (destination.id == R.id.nav_camera && pendingCaptureAfterNavigation) {
                homeFragment?.captureAdditionalPhoto()
                pendingCaptureAfterNavigation = false
            }
        }
    }

    private fun setupBottomNavigation() {
        photoBtn = findViewById(R.id.btn_photoselection)
        cameraBtn = findViewById(R.id.btn_camera)
        transectsBtn = findViewById(R.id.btn_transects)
        cameraButtonIcon = findViewById(R.id.btn_camera_icon)

        photoBtn?.setOnClickListener {
            try {
                getCurrentHomeFragment()?.stopPhotoCapture()
                safeNavigate(R.id.nav_photoselection)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error navigating to photo selection", e)
                Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        cameraBtn?.setOnClickListener {
            try {
                val currentDest = navController.currentDestination?.id
                if (currentDest == R.id.nav_camera) {
                    // Already on camera: toggle capture
                    val homeFragment = getCurrentHomeFragment()
                    if (homeFragment != null) {
                        if (homeFragment.homeViewModel.isCapturing.value == true) {
                            homeFragment.stopPhotoCapture()
                        } else {
                            homeFragment.captureAdditionalPhoto()
                        }
                    }
                } else {
                    pendingCaptureAfterNavigation = true
                    safeNavigate(R.id.nav_camera)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error with camera button", e)
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        transectsBtn?.setOnClickListener {
            try {
                getCurrentHomeFragment()?.stopPhotoCapture()
                safeNavigate(R.id.nav_transects)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error navigating to transects", e)
                Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun setCameraButtonIcon(isCapturing: Boolean) {
        cameraButtonIcon?.setImageResource(
            if (isCapturing) R.drawable.ic_stop else R.drawable.ic_menu_camera
        )
        cameraBtn?.alpha = if (isCapturing) 0.5f else 1.0f
    }

    private fun safeNavigate(destId: Int) {
        try {
            val current = navController.currentDestination?.id
            if (current != destId) {
                navController.navigate(destId)
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.close()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation failed to destination $destId", e)
            Toast.makeText(this, "Navigation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBottomSelection(destinationId: Int) {
        photoBtn?.isSelected = destinationId == R.id.nav_photoselection
        cameraBtn?.isSelected = destinationId == R.id.nav_camera
        transectsBtn?.isSelected = destinationId == R.id.nav_transects
    }

    private fun applyColorMode() {
        val logoGreen = ColorModeManager.getLogoGreen(this)
        val logoDarkGreen = ColorModeManager.getLogoDarkGreen(this)

        binding.navView.setBackgroundColor(logoGreen)

        val navHeaderView = binding.navView.getHeaderView(0)
        val navHeaderLayout = navHeaderView?.findViewById<LinearLayout>(R.id.nav_header_layout)
        navHeaderLayout?.setBackgroundColor(logoDarkGreen)

        binding.appBarMain.toolbar.setBackgroundColor(logoGreen)

        val outerContainer = binding.appBarMain.root.getChildAt(0) as? LinearLayout
        outerContainer?.setBackgroundColor(logoDarkGreen)

        val innerContainer = outerContainer?.getChildAt(0) as? LinearLayout
        innerContainer?.setBackgroundColor(logoGreen)

        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)
        bottomNav?.setBackgroundColor(logoDarkGreen)

        val photoselectionBtn = findViewById<LinearLayout>(R.id.btn_photoselection)
        val cameraBtnView = findViewById<LinearLayout>(R.id.btn_camera)
        val transectsBtnView = findViewById<LinearLayout>(R.id.btn_transects)

        photoselectionBtn?.setBackgroundColor(logoGreen)
        cameraBtnView?.setBackgroundColor(logoGreen)
        transectsBtnView?.setBackgroundColor(logoGreen)
    }

    private fun initializeDatabase() {
        val sharedPrefs = getSharedPreferences("ButterflyApp", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val butterflies = loadButterfliesFromJson()
                    val database = ButterflyDatabase.getDatabase(applicationContext)
                    database.butterflyDao().insertAll(butterflies)

                    sharedPrefs.edit { putBoolean("isFirstLaunch", false) }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Butterfly database initialized with ${butterflies.size} species",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error initializing database", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error loading butterfly data: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun loadButterfliesFromJson(): List<ButterflyEntity> {
        val butterflies = mutableListOf<ButterflyEntity>()
        try {
            val jsonString = assets.open("butterflies.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val butterfly = ButterflyEntity(
                    id = jsonObject.getInt("id"),
                    name = jsonObject.getString("name"),
                    species = jsonObject.getString("species"),
                    imageFile = jsonObject.getString("imageFile"),
                    description = jsonObject.getString("description"),
                    habitat = jsonObject.getString("habitat"),
                    wingspan = jsonObject.getString("wingspan"),
                    flightPeriod = jsonObject.getString("flightPeriod"),
                    isFavorite = jsonObject.optBoolean("isFavorite", false)
                )
                butterflies.add(butterfly)
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Error loading butterflies from JSON", e)
        }
        return butterflies
    }

    private fun getCurrentHomeFragment(): HomeFragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull { it is HomeFragment } as? HomeFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val settingsItem = menu.findItem(R.id.action_settings)
        val isColorblind = ColorModeManager.isColorblindMode(this)
        settingsItem?.title = if (isColorblind) {
            "Settings (Colorblind Mode: ON)"
        } else {
            "Settings (Colorblind Mode: OFF)"
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val newMode = ColorModeManager.toggleColorblindMode(this)
                val message = if (newMode) {
                    "Colorblind mode enabled"
                } else {
                    "Colorblind mode disabled"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                recreate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}