package com.example.butterflydetector

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_camera,
                R.id.nav_photoselection,
                R.id.nav_speciescatalog,
                R.id.nav_transects,
                R.id.nav_transectwalks
            ),
            drawerLayout
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

    // Liefert die momentan aktive HomeFragment-Instanz (falls vorhanden)
    private fun getCurrentHomeFragment(): HomeFragment? {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        return navHost?.childFragmentManager?.fragments?.firstOrNull { it is HomeFragment } as? HomeFragment
    }

    // Vom HomeFragment gerufen, um das Kamera-Icon zu aktualisieren
    override fun setCameraButtonIcon(isCapturing: Boolean) {
        cameraButtonIcon?.setImageResource(if (isCapturing) R.drawable.ic_stop else R.drawable.ic_menu_camera)
        // Wir setzen alpha nur, wenn wir auf Camera sind; ansonsten wird es vom Fragment gesteuert.
        cameraBtn?.alpha = if (isCapturing) 0.5f else 1.0f
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
