package com.myapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

public class PeriodicService extends Service {

    private static final String TAG = "MyApp.PeriodicService";
    private static PowerManager.WakeLock wakeLock = null;
    public static boolean isServiceStarted = false;
    private static HandlerThread heartbeatThread;
    private static Handler heartbeatHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) {
            Telegram.Log("onStartCommand called: " + intent.getAction());
            startService();
        } else {
            Telegram.Log("onStartCommand called: no intent");
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        Telegram.Log("OnCreate called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = createNotification();
            if(notification != null) {
                Telegram.Log("notification is not null");
                startForeground(1, notification);
            } else {
                Telegram.Log("notification is null");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Telegram.Log("OnDestroy called");
        Log.i(TAG, "onDestroy");
        stopService();
    }

    @Override
    public boolean stopService(Intent name) {
        Telegram.Log("Stop service intent: " + name.getAction());
        Log.i(TAG, "Stop service intent: " + name.getAction());
        stopService();
        return super.stopService(name);
    }

    private void startService() {
        if (isServiceStarted) {
            Telegram.Log("starting service, but service already started");
            return;
        }
        Telegram.Log("Starting service");
        Log.i(TAG, "startService");
        isServiceStarted = true;

        Log.i(TAG, "heartbeatThread: " + heartbeatThread);
        if(heartbeatThread != null) {
            heartbeatThread.interrupt();
            heartbeatThread.quit();
            Log.i(TAG, "quit heartbeatThread");
        }

        heartbeatThread = new HandlerThread("HeartbeatThread");
        heartbeatThread.start();
        Looper heartbeatLooper = heartbeatThread.getLooper();
        heartbeatHandler = new Handler(heartbeatLooper);
        heartbeatHandler.post(this::heartbeat);
    }

    private synchronized static void releaseWakeLock() {
        Telegram.Log("Releasing wake lock");
        try {
            if(wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch(Throwable t) {
            if(t.getMessage() != null) {
                Log.e(TAG, t.getMessage());
            }
            t.printStackTrace();
        }
    }

    private void heartbeat() {
        Log.i(TAG, "heartbeat!");
        Telegram.Log("Heartbeat!");
        if(!isServiceStarted) return;
        releaseWakeLock();
        PowerManager pw = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(pw == null) return;
        wakeLock = pw.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeartbeatService::lock");
        Telegram.Log("Acquiring wake lock");
        wakeLock.acquire(PeriodicModule.heartbeatIntervalSec * 1000L);

        try {
            heartbeatHandler.post(PeriodicModule::heartbeat);
            heartbeatHandler.postDelayed(this::heartbeat, PeriodicModule.heartbeatIntervalSec * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopService() {
        Log.i(TAG, "stopService");
        Telegram.Log("Stopping PeriodicService");
        try {
            releaseWakeLock();
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.w(TAG, "Service stopped without being started: " + e.getMessage());
        }
        isServiceStarted = false;
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;

        String channelId = "PeriodicService";

        Intent notificationIntent = new Intent(this, MainActivity.class);
        int mutableFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                mutableFlag
        );
        return new Notification.Builder(this, channelId)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("My sticky foreground notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();

    }

}
