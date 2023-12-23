package com.av.arthanfinance

import android.app.Application

import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.facebook.FacebookSdk;

class MyApplication : Application() {

    override fun onCreate() {
// Must be called before super.onCreate()
		ActivityLifecycleCallback.register(this)   //added by CleverTap Assistant
        super.onCreate()
    }
}
