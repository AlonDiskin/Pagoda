package com.diskin.alon.pagoda.home.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Application home screen.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var graphProvider: AppHomeNavProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set navigation drawer listener
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Set toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set navigation
        val finalHost = NavHostFragment.create(graphProvider.getAppNavGraph())
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_container, finalHost)
            .setPrimaryNavigationFragment(finalHost) // equivalent to app:defaultNavHost="true"
            .commit()
    }

    override fun onStart() {
        super.onStart()
        // Set toolbar with navigation ui
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navController = findNavController(R.id.nav_host_container)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_container)
                    .navigate(graphProvider.getSettingsDestId())
                true
            }

            R.id.action_search_location -> {
                findNavController(R.id.nav_host_container)
                    .navigate(graphProvider.getSearchLocationsDestId())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_container).navigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        closeDrawer()
        when (item.itemId) {
            R.id.nav_home -> {
                val controller = findNavController(R.id.nav_host_container)
                val homeDest = controller.graph.startDestination

                controller.popBackStack(homeDest,true)
                controller.navigate(homeDest)
            }
        }
        return true
    }

    private fun closeDrawer() {
        Thread {
            Thread.sleep(300)
            runOnUiThread {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }.start()
    }

    private fun isDrawerOpen(): Boolean {
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        return drawer.isDrawerOpen(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }
}