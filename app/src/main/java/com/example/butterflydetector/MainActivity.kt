package com.example.butterflydetector

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.butterflydetector.databinding.ActivityMainBinding
import com.example.butterflydetector.ui.home.HomeFragment
import com.example.butterflydetector.data.ButterflyDatabase
import com.example.butterflydetector.data.ButterflyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        println("DEBUG: MainActivity onCreate reached")

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        initializeDatabase()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_photoselection, R.id.nav_speciescatalog, R.id.nav_transects, R.id.nav_transectwalks
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menuItem = when (destination.id) {
                R.id.nav_home -> navView.menu.findItem(R.id.nav_home)
                R.id.nav_photoselection -> navView.menu.findItem(R.id.nav_photoselection)
                R.id.nav_speciescatalog -> navView.menu.findItem(R.id.nav_speciescatalog)
                R.id.nav_transects -> navView.menu.findItem(R.id.nav_transects)
                R.id.nav_transectwalks -> navView.menu.findItem(R.id.nav_transectwalks)
                else -> null
            }

            for (i in 0 until navView.menu.size()) {
                navView.menu.getItem(i).isChecked = false
            }

            menuItem?.isChecked = true
        }

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
                else -> false
            }
        }

        setupBottomNavigation(navController)
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

    private fun setupBottomNavigation(navController: androidx.navigation.NavController) {
        val photoselectionBtn = findViewById<LinearLayout>(R.id.btn_photoselection)
        val cameraBtn = findViewById<LinearLayout>(R.id.btn_camera)
        val transectsBtn = findViewById<LinearLayout>(R.id.btn_transects)

        photoselectionBtn?.setOnClickListener {
            getCurrentHomeFragment()?.stopPhotoCapture()
            navController.navigate(R.id.nav_photoselection)
        }

        cameraBtn?.setOnClickListener {
            val currentFragment = getCurrentHomeFragment()
            if (currentFragment != null) {
                currentFragment.captureAdditionalPhoto()
            } else {
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
