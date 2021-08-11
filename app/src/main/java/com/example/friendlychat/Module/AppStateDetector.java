package com.example.friendlychat.Module;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleInitializer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

/* In my chat app I want to know when the user close the app (navigate to another app, or
* go the home screen so I can save their last time activation my firestore server,
* this class demonstrates the process*/

public class AppStateDetector extends androidx.multidex.MultiDexApplication implements
        LifecycleObserver {
    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void created() {
        Log.d("SampleLifeCycle", "ON_CREATE");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void started() {
        Log.d("SampleLifeCycle", "ON_START");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resumed() {
        Log.d("SampleLifeCycle", "ON_RESUME");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void paused() {
        Log.d("SampleLifeCycle", "ON_PAUSE");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopped() {
        Log.d("SampleLifeCycle", "ON_STOP");
    }


}
