package com.example.butterflydetector

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.butterflydetector.databinding.ActivityMainBinding
import com.example.butterflydetector.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        //tutorialFirstStart()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_photoselection,
                R.id.nav_speciescatalog,
                R.id.nav_transects,
                R.id.nav_transectwalks
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Update drawer menu selection based on current destination
            val menuItem = when (destination.id) {
                R.id.nav_home -> navView.menu.findItem(R.id.nav_home)
                R.id.nav_photoselection -> navView.menu.findItem(R.id.nav_photoselection)
                R.id.nav_speciescatalog -> navView.menu.findItem(R.id.nav_speciescatalog)
                R.id.nav_transects -> navView.menu.findItem(R.id.nav_transects)
                R.id.nav_transectwalks -> navView.menu.findItem(R.id.nav_transectwalks)
                R.id.nav_tutorial -> navView.menu.findItem(R.id.nav_tutorial)
                else -> null
            }

            // Clear all selections first
            for (i in 0 until navView.menu.size()) {
                navView.menu.getItem(i).isChecked = false
            }

            // Set the current destination as checked
            menuItem?.isChecked = true
        }

        // Custom navigation item selection listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
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
        // Find bottom navigation buttons
        val photoselectionBtn = findViewById<LinearLayout>(R.id.btn_photoselection)
        val cameraBtn = findViewById<LinearLayout>(R.id.btn_camera)
        val transectsBtn = findViewById<LinearLayout>(R.id.btn_transects)

        // Set click listeners for bottom navigation
        photoselectionBtn?.setOnClickListener {
            getCurrentHomeFragment()?.stopPhotoCapture()
            navController.navigate(R.id.nav_photoselection)
        }

        cameraBtn?.setOnClickListener {
            val currentFragment = getCurrentHomeFragment()
            if (currentFragment != null) {
                currentFragment.captureAdditionalPhoto()
            } else {
                // Navigate to home if not already there
                navController.navigate(R.id.nav_home)
            }
        }

        transectsBtn?.setOnClickListener {
            navController.navigate(R.id.nav_transects)
        }
    }

    private fun getCurrentHomeFragment(): HomeFragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull { it is HomeFragment } as? HomeFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                // Replace previous FAB behavior
                Snackbar.make(binding.appBarMain.toolbar, "Information about Butterfly Detector", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
