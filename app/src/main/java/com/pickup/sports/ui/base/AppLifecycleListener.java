package com.pickup.sports.ui.base;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.pickup.sports.utils.SocketHandler;


//public class AppLifecycleListener implements DefaultLifecycleObserver {
//    Context context;
//
//    public AppLifecycleListener(Context context) {
//        this.context = context;
//    }
//
//    @Override
//    public void onStart(@NonNull LifecycleOwner owner) {
//        DefaultLifecycleObserver.super.onStart(owner);
//        Log.i("ONSTART", "DefaultLifecycleObserver: ");
//
//    }
//
//    @Override
//    public void onStop(@NonNull LifecycleOwner owner) {
//        DefaultLifecycleObserver.super.onStop(owner);
//        Log.i("onStop", "DefaultLifecycleObserver: ");
//    }
//
//    @Override
//    public void onDestroy(@NonNull LifecycleOwner owner) {
//        DefaultLifecycleObserver.super.onDestroy(owner);
//        Log.i("onDestroy", "DefaultLifecycleObserver: ");
//        SocketHandler.closeConnection()
//    }
//}

public class AppLifecycleListener implements DefaultLifecycleObserver {
    private final Context context;
    private boolean isAppBackgrounded = false;

    public AppLifecycleListener(Context context) {
        this.context = context;

    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Log.i("ONSTART", "App moved to foreground");
        isAppBackgrounded = false; // App is now in foreground
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        Log.i("ONSTOP", "App moved to background");
        isAppBackgrounded = true;

//        // Delay socket disconnection to check if the app is truly being killed
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (isAppBackgrounded) {
//                Log.i("APP_EXIT", "App is being terminated, closing socket.");
//                SocketHandler.INSTANCE.closeConnection();
//            }
//        }, 7000); // Adjust delay if needed
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        Log.d("ONDESTROY", "App is being destroyed");
        SocketHandler.INSTANCE.closeConnection();
    }
}