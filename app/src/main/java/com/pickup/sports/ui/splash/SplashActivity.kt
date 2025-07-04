package com.pickup.sports.ui.splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pickup.sports.R
import com.pickup.sports.databinding.ActivitySplashBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.location.LocationHandler
import com.pickup.sports.ui.base.location.LocationResultListener
import com.pickup.sports.ui.base.permission.PermissionHandler
import com.pickup.sports.ui.base.permission.Permissions
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import com.pickup.sports.ui.home_modules.location_module.LocationActivity
import com.pickup.sports.utils.ImageUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() , LocationResultListener {
    private val  viewModel : SplashActivityVm by viewModels()
    private var locationHandler: LocationHandler? = null
    private var mCurrentLocation: Location? = null
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun getLayoutResource(): Int {
        return R.layout.activity_splash
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
     }

    override fun onCreateView() {
        ImageUtils.getStatusBarColor(this@SplashActivity , R.color.black )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        checkLocation()
        checkNotificationPermission()

    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request notification permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("WelcomeActivity", "Notification permission granted")
            } else {
                Log.d("WelcomeActivity", "Notification permission denied")
            }
        }
    }


    private fun checkLocation() {
        Permissions.check(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            0,
            object : PermissionHandler() {
                override fun onGranted() {
                    createLocationHandler()
                    //   initHandler()
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    super.onDenied(context, deniedPermissions)
                    checkLocation()
                }
            })
    }


    private fun createLocationHandler() {
        locationHandler = LocationHandler(this, this)
        locationHandler?.getUserLocation()
        locationHandler?.removeLocationUpdates()
    }

    private fun initHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeDashBoardActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3000 is the delayed time in milliseconds.
    }

    override fun getLocation(location: Location) {
        this.mCurrentLocation = location
        val latitude = location.latitude
        val longitude = location.longitude

        LocationActivity.lat = latitude
        LocationActivity.long = longitude

        HomeFragment.lat = latitude
        HomeFragment.long = longitude

        GameDetailsActivity.lat = latitude
        GameDetailsActivity.long = longitude

        HomeDashBoardActivity.lat = latitude
        HomeDashBoardActivity.long = longitude



        initHandler()

    }


}