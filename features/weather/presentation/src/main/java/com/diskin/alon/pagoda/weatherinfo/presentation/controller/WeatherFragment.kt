package com.diskin.alon.pagoda.weatherinfo.presentation.controller

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.databinding.FragmentWeatherBinding
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

/**
 * Shows location weather data.
 */
@OptionalInject
@AndroidEntryPoint
class WeatherFragment(
    registry: ActivityResultRegistry? = null
) : Fragment(){

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: FragmentWeatherBinding
    private val locationPermissionLauncher = createActivityResultLauncher(RequestPermission(),registry) {
        it?.let { granted ->
            when(granted) {
                true -> viewModel.refresh()
                false -> showToast(getString(R.string.location_permission_error))
            }
        }
    }
    private val locationSettingLauncher = createActivityResultLauncher(StartIntentSenderForResult(),registry) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> viewModel.refresh()
            else -> showToast(getString(R.string.device_location_error))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setFragmentResultListener(getString(R.string.locaiton_request_key)) { _, bundle ->
            val lat = bundle.getDouble(getString(R.string.arg_lat_key))
            val lon = bundle.getDouble(getString(R.string.arg_lon_key))

            showTopLayout { viewModel.loadLocationWeather(lat, lon) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set refresh color
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.green_900)
        )

        // Setup forecast adapters
        val hourlyForeCastAdapter = HourlyForecastAdapter()
        val dailyForecastAdapter = DailyForecastAdapter()
        binding.hourForecast.adapter = hourlyForeCastAdapter
        binding.dailyForecast.adapter = dailyForecastAdapter

        // Observe view model weather data state
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                binding.weather = it
                hourlyForeCastAdapter.submitList(it.hourlyForecast)
                dailyForecastAdapter.submitList(it.dailyForecast)
                binding.weatherMain.root.visibility = View.VISIBLE
                binding.nestedScrollView.visibility = View.VISIBLE
            }
        }

        // Observe view model weather data update state
        viewModel.update.observe(viewLifecycleOwner) { update ->
            when(update) {
                is UpdateViewData.Refresh -> binding.swipeRefresh.isRefreshing = true
                is UpdateViewData.EndRefresh -> binding.swipeRefresh.isRefreshing = false
            }
        }

        // Observe view model weather error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { resolveWeatherDataError(it) }
        }

        // Handle swipe to refresh
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Condition swipe to refresh action to app bar layout being expanded
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            binding.swipeRefresh.isEnabled = (verticalOffset == 0)
        })

        // Show location name as appbar title, when appbar collapses
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val showNameOffset = -1 * binding.weatherMain.root.height
            if (verticalOffset == showNameOffset) {
                requireActivity()
                binding.weather?.let { (requireActivity() as AppCompatActivity).supportActionBar?.title = it.locationName }
            } else {
                binding.weather?.let { (requireActivity() as AppCompatActivity).supportActionBar?.title = "" }
            }
        })

        //  Observe view model current location weather indicator
        viewModel.isCurrentLocationWeather.observe(viewLifecycleOwner) {
            binding.weatherMain.locationIndicator.visibility = when (it) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_weather, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_locations -> {
                findNavController().navigate(R.id.locationsFragment)
                true
            }

            R.id.action_current_location_weather -> {
                showTopLayout { viewModel.loadCurrentLocationWeather() }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun <I,O> createActivityResultLauncher(
        contract: ActivityResultContract<I,O>,
        registry: ActivityResultRegistry? = null,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return when(registry) {
            null -> registerForActivityResult(contract,callback)
            else -> registerForActivityResult(contract,registry, callback)
        }
    }

    private fun resolveWeatherDataError(appError: AppError) {
        when(appError.type) {
            ErrorType.UNKNOWN_ERR -> showToast(getString(R.string.unknown_error))
            ErrorType.DEVICE_NETWORK -> showToast(getString(R.string.device_network_error))
            ErrorType.REMOTE_SERVER -> showToast(getString(R.string.remote_server_error))
            ErrorType.DEVICE_LOCATION -> askUserToTurnOnDeviceLocation(appError)
            ErrorType.LOCATION_PERMISSION -> askUserForLocationPermission()
        }
    }

    private fun askUserForLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun askUserToTurnOnDeviceLocation(appError: AppError) {
        val errorOrigin = appError.origin
        if (errorOrigin is ResolvableApiException) {
            val intentSenderRequest = IntentSenderRequest.Builder(errorOrigin.resolution).build()
            locationSettingLauncher.launch(intentSenderRequest)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(),
            message,
            Toast.LENGTH_LONG)
            .show()
    }

    private fun showTopLayout(action: () -> (Unit)) {
        binding.nestedScrollView.post{
            binding.nestedScrollView.scrollTo(0,0)
            binding.appBar.setExpanded(true)
            action.invoke()
        }
    }
}
