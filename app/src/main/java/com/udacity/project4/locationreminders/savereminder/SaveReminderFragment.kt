package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value


            var requestId: String = "error"

            if (!(title.isNullOrEmpty()) && !(location.isNullOrEmpty())) {

                requestId = _viewModel.validateAndSaveReminder(
                        ReminderDataItem(title, description, location,
                        latitude, longitude
                ))

            }
            if (!requestId.equals("error")){
                Log.i(TAG, "values before call ${_viewModel.reminderTitle.value}")
                addGeofence(requestId)
            }
//             use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(requestId: String) {

    val permFine = checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val permBack = checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        Log.i(TAG, "fine? ${permFine == PackageManager.PERMISSION_GRANTED}, " +
                "background? ${permBack == PackageManager.PERMISSION_GRANTED}")

        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value
        if (latitude != null && longitude != null) {
            val geofence = Geofence.Builder()
                    .setRequestId(requestId)
                    .setCircularRegion(latitude, longitude,
                            26.0F
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = ACTION_GEOFENCE_EVENT

            val geofencePendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                addOnCompleteListener {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                        addOnSuccessListener {
                            Log.i(TAG, geofencingRequest.geofences[0].toString())
                            Log.i(TAG, "added gf: $latitude, $longitude")
                        }

                        addOnFailureListener {
                            if (it.message != null) {
                                Log.i(TAG, it.message.toString())
                            }
                        }

                }
            }


            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
private const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
private const val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = 3600000
private const val TAG = "SaveReminderFragment"