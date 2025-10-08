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

class MainActivity : AppCompatActivity(), HomeFragment.CameraButtonController {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    // Bottom nav custom views
    private var photoBtn: LinearLayout? = null
    private var cameraBtn: LinearLayout? = null
    private var transectsBtn: LinearLayout? = null
    private var cameraButtonIcon: ImageView? = null

    private var pendingCaptureAfterNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        println("DEBUG: MainActivity onCreate reached")

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Apply colors based on current mode
        applyColorMode()

        //tutorialFirstStart()

        initializeDatabase()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        // TODO   val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_camera, R.id.nav_photoselection, R.id.nav_speciescatalog, R.id.nav_transects, R.id.nav_transectwalks, R.id.nav_tutorial
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Bind burger menu (NavigationView) to NavController so drawer items navigate
        navView.setupWithNavController(navController)
        // WICHTIG: Bottom navbar custom views erst nach Layout-Inflation finden

        findViewById<View>(android.R.id.content).post {
            photoBtn = findViewById(R.id.btn_photoselection)
            cameraBtn = findViewById(R.id.btn_camera)
            transectsBtn = findViewById(R.id.btn_transects)
            cameraButtonIcon = findViewById(R.id.btn_camera_icon)


            // Bottom buttons: Photo
            photoBtn?.setOnClickListener {
                // Wenn wir von Camera weg navigieren, stoppe Capture
                getCurrentHomeFragment()?.stopPhotoCapture()
                safeNavigate(R.id.nav_photoselection)
            }

            // Bottom buttons: Camera
            cameraBtn?.setOnClickListener {
                val currentDest = navController.currentDestination?.id
                if (currentDest == R.id.nav_camera) {
                    // Bereits auf Camera: direkt Foto / Stop
                    val homeFragment = getCurrentHomeFragment()
                    if (homeFragment != null) {
                        if (homeFragment.homeViewModel.isCapturing.value == true) homeFragment.stopPhotoCapture()
                        else homeFragment.captureAdditionalPhoto()
                    }
                } else {
                    // Navigiere zu Camera und löse Aufnahme nach Navigation aus
                    pendingCaptureAfterNavigation = true
                    safeNavigate(R.id.nav_camera)
                }
            }


            // Bottom buttons: Transects
            transectsBtn?.setOnClickListener {
                getCurrentHomeFragment()?.stopPhotoCapture()
                safeNavigate(R.id.nav_transects)
            }
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menuItem = when (destination.id) {
                R.id.nav_camera -> navView.menu.findItem(R.id.nav_camera)
                R.id.nav_photoselection -> navView.menu.findItem(R.id.nav_photoselection)
                R.id.nav_speciescatalog -> navView.menu.findItem(R.id.nav_speciescatalog)
                R.id.nav_transects -> navView.menu.findItem(R.id.nav_transects)
                R.id.nav_transectwalks -> navView.menu.findItem(R.id.nav_transectwalks)
                R.id.nav_tutorial -> navView.menu.findItem(R.id.nav_tutorial)
                else -> null
            }

            for (i in 0 until navView.menu.size()) {
                navView.menu.getItem(i).isChecked = false
            }

            menuItem?.isChecked = true
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_camera -> {
                    navController.navigate(R.id.nav_camera)
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_photoselection -> {
                    navController.navigate(R.id.nav_photoselection)
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_speciescatalog -> {
                    navController.navigate(R.id.nav_speciescatalog)
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_transects -> {
                    navController.navigate(R.id.nav_transects)
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_transectwalks -> {
                    navController.navigate(R.id.nav_transectwalks)
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_tutorial -> {
                    navController.navigate(R.id.nav_tutorial)
                    drawerLayout.closeDrawers()
                    true
                }

                else -> false
            }
        }

        setupBottomNavigation(navController)
    }

    // Vom HomeFragment gerufen, um das Kamera-Icon zu aktualisieren
    override fun setCameraButtonIcon(isCapturing: Boolean) {
        cameraButtonIcon?.setImageResource(if (isCapturing) R.drawable.ic_stop else R.drawable.ic_menu_camera)
        // Wir setzen alpha nur, wenn wir auf Camera sind; ansonsten wird es vom Fragment gesteuert.
        cameraBtn?.alpha = if (isCapturing) 0.5f else 1.0f
    }

    // Helper: sichere Navigation (vermeidet Navigate-to-same-destination)
    private fun safeNavigate(destId: Int) {
        val current = navController.currentDestination?.id
        if (current != destId) {
            navController.navigate(destId)
            // Falls Drawer offen: schließen (optional)
            if (binding.drawerLayout.isOpen) binding.drawerLayout.close()
        }
    }

    // Markiere die aktuell gewählte Bottom-Navigation (nur visuelle Hilfestellung)
    private fun updateBottomSelection(destinationId: Int) {
        photoBtn?.isSelected = destinationId == R.id.nav_photoselection
        cameraBtn?.isSelected = destinationId == R.id.nav_camera
        transectsBtn?.isSelected = destinationId == R.id.nav_transects
        // Hinweis: Kamera-Icon-Alpha wird vom Fragment (isCapturing) gesteuert via setCameraButtonIcon()
    }

    private fun applyColorMode() {
        val logoGreen = ColorModeManager.getLogoGreen(this)
        val logoDarkGreen = ColorModeManager.getLogoDarkGreen(this)

        // Apply to navigation view
        binding.navView.setBackgroundColor(logoGreen)

        val navHeaderView = binding.navView.getHeaderView(0)
        val navHeaderLayout = navHeaderView?.findViewById<LinearLayout>(R.id.nav_header_layout)
        navHeaderLayout?.setBackgroundColor(logoDarkGreen)

        // Apply to toolbar background
        binding.appBarMain.toolbar.setBackgroundColor(logoGreen)

        // Apply to app bar containers
        val outerContainer = binding.appBarMain.root.getChildAt(0) as? LinearLayout
        outerContainer?.setBackgroundColor(logoDarkGreen)

        val innerContainer = outerContainer?.getChildAt(0) as? LinearLayout
        innerContainer?.setBackgroundColor(logoGreen)

        // Apply to bottom navigation
        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)
        bottomNav?.setBackgroundColor(logoDarkGreen)

        // Apply to bottom navigation buttons
        val photoselectionBtn = findViewById<LinearLayout>(R.id.btn_photoselection)
        val cameraBtn = findViewById<LinearLayout>(R.id.btn_camera)
        val transectsBtn = findViewById<LinearLayout>(R.id.btn_transects)

        photoselectionBtn?.setBackgroundColor(logoGreen)
        cameraBtn?.setBackgroundColor(logoGreen)
        transectsBtn?.setBackgroundColor(logoGreen)
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
        // TODO THIS CODE SECTION DOES NOT BELONG HERE , IT BELONGS TO onCreateView()
                    // Bind burger menu (NavigationView) to NavController so drawer items navigate
        // navView.setupWithNavController(navController)

                    sharedPrefs.edit().putBoolean("isFirstLaunch", false).apply()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Butterfly database initialized with ${butterflies.size} species",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
            e.printStackTrace()
        }
        return butterflies
    }

    private fun tutorialFirstStart() {

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        if (firstStart) {
            Log.d("firstLaunch", "before edit")
            //   startActivity(Intent(this, TutorialFragment::class.java))
            prefs.edit().putBoolean("firstStart", false).apply()

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.appBarMain.toolbar)
        }
    }

    private fun setupBottomNavigation(navController: androidx.navigation.NavController) {
        val photoselectionBtn = findViewById<LinearLayout>(R.id.btn_photoselection)
        val cameraBtn = findViewById<LinearLayout>(R.id.btn_camera)
        val transectsBtn = findViewById<LinearLayout>(R.id.btn_transects)

        photoselectionBtn?.setOnClickListener {
            getCurrentHomeFragment()?.stopPhotoCapture()
            navController.navigate(R.id.nav_photoselection)
        }

            // Bottom buttons: Camera
            cameraBtn?.setOnClickListener {
                val currentDest = navController.currentDestination?.id
                if (currentDest == R.id.nav_camera) {
                    // Bereits auf Camera: direkt Foto / Stop
                    val homeFragment = getCurrentHomeFragment()
                    if (homeFragment != null) {
                        if (homeFragment.homeViewModel.isCapturing.value == true) homeFragment.stopPhotoCapture()
                        else homeFragment.captureAdditionalPhoto()
                    }
                } else {
                    // Navigiere zu Camera und löse Aufnahme nach Navigation aus
                    pendingCaptureAfterNavigation = true
                    safeNavigate(R.id.nav_camera)
                }
            }

        transectsBtn?.setOnClickListener {
            navController.navigate(R.id.nav_transects)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Bottom-Navbar visuell aktualisieren
            updateBottomSelection(destination.id)

            val homeFragment = getCurrentHomeFragment()

            // Wenn man weg vom Camera-Fragment navigiert → Aufnahme stoppen
            if (destination.id != R.id.nav_camera) {
                homeFragment?.stopPhotoCapture()
            }

            // Wenn man zum Camera-Fragment navigiert
            if (destination.id == R.id.nav_camera) {
                // Wenn der Wechsel über Bottom-Nav oder Drawer kam → Kamera starten
                if (pendingCaptureAfterNavigation) {
                    homeFragment?.captureAdditionalPhoto()
                    pendingCaptureAfterNavigation = false
                } else {
                    // Wenn über Drawer direkt ausgewählt → Fotoaufnahme starten
                    homeFragment?.captureAdditionalPhoto()
                }
            }
        }
    }

    private fun getCurrentHomeFragment(): HomeFragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull { it is HomeFragment } as? HomeFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        // Update the settings menu item to show current mode
        val settingsItem = menu.findItem(R.id.action_settings)
        val isColorblind = ColorModeManager.isColorblindMode(this)
        settingsItem?.title = if (isColorblind) "Settings (Colorblind Mode: ON)" else "Settings (Colorblind Mode: OFF)"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                Snackbar.make(binding.appBarMain.toolbar, "Information about Butterfly Detector", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show()
                true
            }
            R.id.action_settings -> {
                // Toggle colorblind mode
                val newMode = ColorModeManager.toggleColorblindMode(this)
                val message = if (newMode) {
                    "Colorblind mode enabled"
                } else {
                    "Colorblind mode disabled"
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                // Recreate activity to apply new colors
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
