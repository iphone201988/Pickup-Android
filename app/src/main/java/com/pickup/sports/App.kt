package com.pickup.sports

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ProcessLifecycleOwner
import com.pickup.sports.ui.base.AppLifecycleListener
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    private var isRestarting: Boolean = false


    fun onLogout() {
        restartApp()
    }

    override fun onCreate() {
        super.onCreate()


        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(this@App))
                registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
//              SocketHandler.closeConnection()
            }
    })

    }

    private fun restartApp() {
        if (!isRestarting) {
            isRestarting = true
            val intent =
                baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    /* companion object {
         private var instance: App? = null
         fun getInstance(): App? {
             return instance
         }
         fun getToken(): String? {
             return SharedPrefManager.getString(SharedPrefManager.KEY.Token, null)

         }*/

}
