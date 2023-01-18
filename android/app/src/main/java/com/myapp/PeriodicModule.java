package com.myapp;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class PeriodicModule extends ReactContextBaseJavaModule {

    private final static String TAG = "Cuebly.PeriodicModule";
    static int heartbeatIntervalSec = 60;
    private static ReactApplicationContext context;

    PeriodicModule(ReactApplicationContext context) {
        super(context);
        PeriodicModule.context = context;
    }


    @Override
    public String getName() {
        return "Periodic";
    }

    @ReactMethod
    public void start() {
        PeriodicModule.context = getReactApplicationContext();
        Log.i(TAG, "start");
        Telegram.Log("PeriodicModule:start");

        Log.i(TAG, "PeriodicService.isServiceStarted: " + PeriodicService.isServiceStarted);
        if (PeriodicService.isServiceStarted) {
            Log.i(TAG, "service does not need to restart");
            Telegram.Log("PeriodicModule:is already started");
            return;
        }
        stop();
        try {
            Log.i(TAG, "Creating new pending intent");
            Intent periodicServiceIntent = new Intent(context, PeriodicService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(TAG, "Start foreground service");
                context.startForegroundService(periodicServiceIntent);
            } else {
                Log.i(TAG, "Start service");
                context.startService(periodicServiceIntent);
            }
        } catch (Exception e) {
            Log.i(TAG, "Could not start: " + e.getMessage());
        }

    }

    @ReactMethod
    public void stop() {
        Log.i(TAG, "stop");
        Telegram.Log("PeriodicModule:stop");
        PeriodicModule.context = getReactApplicationContext();
        Intent periodicIntent = new Intent(context, PeriodicService.class);

        context.stopService(periodicIntent);
    }

    @ReactMethod
    public void telegram(String text) {
        Telegram.Log(text);
    }

    public static void heartbeat() {
        if (context == null) {
            Telegram.Log("heartbeat|context is null");
            Log.i(TAG, "heartbeat|context is null");
            return;
        }
        try {
            Telegram.Log("heartbeat|emitting");
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("heartbeat", null);
        } catch (Exception e) {
            Log.e(TAG, "heartbeat|" + e.getMessage());
        }
    }
}