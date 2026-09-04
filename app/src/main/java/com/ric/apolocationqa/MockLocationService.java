package com.ric.apolocationqa;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

public class MockLocationService extends Service {
    public static final String ACTION_START = "com.ric.apolocationqa.START";
    public static final String ACTION_STOP = "com.ric.apolocationqa.STOP";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";
    public static final String EXTRA_ACCURACY = "accuracy";

    private static final String CHANNEL_ID = "mock_location";
    private static final int NOTIFICATION_ID = 41;

    private LocationManager locationManager;
    private volatile boolean running;
    private Thread worker;
    private double latitude;
    private double longitude;
    private float accuracy;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        if (ACTION_STOP.equals(intent.getAction())) {
            stopMocking();
            stopSelf();
            return START_NOT_STICKY;
        }

        latitude = intent.getDoubleExtra(EXTRA_LAT, 0.0);
        longitude = intent.getDoubleExtra(EXTRA_LON, 0.0);
        accuracy = intent.getFloatExtra(EXTRA_ACCURACY, 3f);

        startForeground(NOTIFICATION_ID, buildNotification());
        if (!running) {
            running = true;
            worker = new Thread(this::loop, "mock-location-loop");
            worker.start();
        }
        return START_STICKY;
    }

    private void loop() {
        try {
            addProvider(LocationManager.GPS_PROVIDER);
            addProvider(LocationManager.NETWORK_PROVIDER);
            while (running) {
                push(LocationManager.GPS_PROVIDER);
                push(LocationManager.NETWORK_PROVIDER);
                Thread.sleep(1000L);
            }
        } catch (SecurityException se) {
            stopSelf();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            removeProvider(LocationManager.GPS_PROVIDER);
            removeProvider(LocationManager.NETWORK_PROVIDER);
        }
    }

    private void addProvider(String provider) {
        try { locationManager.removeTestProvider(provider); } catch (Exception ignored) {}
        try {
            locationManager.addTestProvider(
                    provider,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(provider, true);
        } catch (IllegalArgumentException ignored) {
            try { locationManager.setTestProviderEnabled(provider, true); } catch (Exception ignored2) {}
        }
    }

    private void push(String provider) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(Math.max(1f, accuracy));
        location.setAltitude(0.0);
        location.setSpeed(0f);
        location.setBearing(0f);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        locationManager.setTestProviderLocation(provider, location);
    }

    private void removeProvider(String provider) {
        try { locationManager.setTestProviderEnabled(provider, false); } catch (Exception ignored) {}
        try { locationManager.removeTestProvider(provider); } catch (Exception ignored) {}
    }

    private void stopMocking() {
        running = false;
        if (worker != null) worker.interrupt();
        removeProvider(LocationManager.GPS_PROVIDER);
        removeProvider(LocationManager.NETWORK_PROVIDER);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "QA location",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows when Android QA mock location is active");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("APO Location QA active")
                .setContentText(String.format(java.util.Locale.US, "%.6f, %.6f", latitude, longitude))
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        stopMocking();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
