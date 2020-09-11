package com.uninorte.a_202030_simpletrackingapp

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uninorte.a_202030_simpletrackingapp.other.TrackingUtility
import com.uninorte.a_202030_workmanager.service.TrackingService
import kotlinx.android.synthetic.main.fragment_tracking.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class TrackingFragment : Fragment(R.layout.fragment_tracking), EasyPermissions.PermissionCallbacks {

    private var thePoints = mutableListOf<Location>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()

        buttonStart.setOnClickListener { view ->
            sendCommandToService("ACTION_START_OR_RESUME_SERVICE")
        }

        buttonStop.setOnClickListener { view ->
            sendCommandToService("ACTION_STOP_SERVICE")
        }

        TrackingService.pathPoint.observe(viewLifecycleOwner, Observer {
            thePoints.add(it)
        })


    }

    private fun sendCommandToService(action: String) =
            Intent(requireContext(), TrackingService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.",
                    0,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.",
                    0,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}