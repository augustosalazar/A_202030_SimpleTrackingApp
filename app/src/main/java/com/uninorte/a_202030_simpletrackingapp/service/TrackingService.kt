package com.uninorte.a_202030_workmanager.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.uninorte.a_202030_simpletrackingapp.MainActivity
import com.uninorte.a_202030_simpletrackingapp.R
import com.uninorte.a_202030_simpletrackingapp.other.TrackingUtility


// LifecycleService enables the context
class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<MutableList<Location>>()
        val pathPoint = MutableLiveData<Location>()
        val thePoints = mutableListOf<Location>()
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                "ACTION_START_OR_RESUME_SERVICE" -> {
                    if (isFirstRun){
                        Log.d("MyOut","start service")
                        isFirstRun = false
                        startForegroundService()
                    } else{
                        Log.d("MyOut","resumed service")
                    }
                }
                "ACTION_PAUSE_SERVICE" -> {
                    Log.d("MyOut","Paused service")
                }
                "ACTION_STOP_SERVICE" -> {
                    Log.d("MyOut","Stopped service")
                    isTracking.postValue(false)
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = 5000L
                    fastestInterval = 2000L
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value!!){
                p0?.locations?.let { locations ->
                    for(location in locations) {
                        Log.d("MyOut","NEW LOCATION: ${location.latitude}, ${location.longitude}")
                        thePoints.add(location)
                        pathPoints.postValue((thePoints))
                        pathPoint.postValue(location)
                    }
                }
            }
        }
    }

    private fun startForegroundService() {

        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID")
            .setAutoCancel(false) // can not be cancelled
            .setOngoing(true) // can not be swap away
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
            .setContentTitle("Tracking App")
            .setContentText("Tracking is ON")
            .setContentIntent(getMainActivityPendingIntent()) // called when notification is touched

        startForeground(2, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = "ACTION_SHOW_TRACKING_FRAGMENT"
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            "NOTIFICATION_CHANNEL_ID",
            "NOTIFICATION_CHANNEL_NAME",
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


}