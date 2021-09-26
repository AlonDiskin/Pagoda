package com.diskin.alon.pagoda.home.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.diskin.alon.pagoda.home.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Application home screen.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var graphProvider: AppHomeNavProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        // Set toolbar
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)

        // Set navigation graph manually since it available only at runtime
        if (savedInstanceState == null) {
            val host = NavHostFragment.create(graphProvider.getAppNavGraph())
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, host)
                .setPrimaryNavigationFragment(host) // equivalent to app:defaultNavHost="true"
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        // Set toolbar with navigation ui
        val navController = findNavController(R.id.nav_host_container)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_container).navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val controller = findNavController(R.id.nav_host_container)

        return when (item.itemId) {
            R.id.action_settings -> {
                controller.navigate(graphProvider.getSettingsDest())
                true
            }
            R.id.action_about -> {
                true
            }
            R.id.action_favorites -> {
                controller.navigate(graphProvider.getFavoritesDest())
                true
            }
            R.id.action_current_location_weather -> {
                val homeDest = controller.graph.startDestination
                controller.popBackStack(homeDest,true)
                controller.navigate(homeDest)
                true
            }
            R.id.action_search -> {
                controller.navigate(graphProvider.getSearchDest())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}