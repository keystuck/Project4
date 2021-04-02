package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Transformations.map
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    private var cameraPosition: CameraPosition? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(context!!, "AIzaSyDq6AdndukFVrcThT1HCvVZfLuT6LNGJP4")
        placesClient = Places.createClient(context!!)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)


    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        Log.i(TAG, "about to bind viewmodel?")
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
//        add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // permissions
        requestForegroundAndBackgroundLocationPermissions()

    checkDeviceLocationSettings(true)
        // fusion? get location (in onMapReady())

//        zoom to the user location after taking his permission

//        add style to the map

//        put a marker to location that the user selected


//        call this function after the user confirms on the selected location


        return binding.root
    }

    override fun onMapReady(p0: GoogleMap?) {
        Log.i(TAG, "in onMapReady")
        if (p0 != null) {
            map = p0
        }

        setMapStyle(map)
        updateLocationUI()
        getDeviceLocation()
        setMapClickListener(map)
    }

    private fun setMapClickListener(map:GoogleMap){

        var latitude = 0.0
        var longitude = 0.0

        var alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage(R.string.confirm)
        alertDialogBuilder.setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener{ dialog, id ->
                    _viewModel.latitude.value = latitude
                    _viewModel.longitude.value = longitude
                    _viewModel.reminderSelectedLocationStr.value = "lat: $latitude, long: $longitude"
                    Log.i(TAG, "adding lat $latitude, long $longitude")
                    onLocationSelected()
                })
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener(){ dialog, id ->
                    map.clear()
                })
        map.setOnMapClickListener { latLng ->
            map.addMarker(
                    MarkerOptions()
                            .position(latLng)
            )
            latitude = latLng.latitude
            longitude = latLng.longitude

            val alert = alertDialogBuilder.create()
            alert.setTitle(R.string.select_location)
            alert.show()
        }

    }


    private fun onLocationSelected() {
        Log.i(TAG, "in onLocationSelected")
        _viewModel.navigationCommand.value = NavigationCommand.Back


        //        When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }

    private fun updateLocationUI(){
        if (map == null){
            return
        }
        try {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                requestForegroundAndBackgroundLocationPermissions()
            }
        } catch (e: SecurityException){
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun getDeviceLocation(){
        try {
            if (foregroundAndBackgroundLocationPermissionApproved()){
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful){
                        lastKnownLocation = task.result
                        Log.i(TAG, "got location: lat ${lastKnownLocation?.latitude}, " +
                                "long ${lastKnownLocation?.longitude}")
                        if (lastKnownLocation != null) {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()
                            ))
                        }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults")
                            Log.e(TAG, "Exception: ${task.exception}")
                            map?.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                            map?.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                }
            } catch (e: SecurityException){
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED))
        {
            Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
            )
                    .setAction(R.string.settings){
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            checkDeviceLocationSettings()
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(requireActivity(),
                            REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                        binding.root,
                        R.string.location_required_error,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok) {
                            checkDeviceLocationSettings()
                        }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                updateLocationUI()
            }
        }
    }

@TargetApi(29)
private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean{
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION))
    val backgroundPermissionApproved =
            if (runningQOrLater){
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
    return foregroundLocationApproved && backgroundPermissionApproved
}

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions(){
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        var resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(
                permissionsArray,
                resultCode
        )
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireActivity(),
                            R.raw.map_style
                    ))
            if (!success){
                Log.e(TAG, "style parsing failed")
            }
        } catch (e: Resources.NotFoundException){
            Log.e(TAG, "can't find style: ${e.message}")
        }
    }

}
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "SelectLocationFragment"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    private const val DEFAULT_ZOOM = 15
    private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    // Keys for storing activity state.
    private const val KEY_CAMERA_POSITION = "camera_position"
    private const val KEY_LOCATION = "location"

    // Used for selecting the current place.
    private const val M_MAX_ENTRIES = 5