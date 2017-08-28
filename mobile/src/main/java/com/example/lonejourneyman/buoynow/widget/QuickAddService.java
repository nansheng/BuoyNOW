package com.example.lonejourneyman.buoynow.widget;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.lonejourneyman.buoynow.MainActivity;
import com.example.lonejourneyman.buoynow.R;
import com.example.lonejourneyman.buoynow.Utility;
import com.example.lonejourneyman.buoynow.data.BuoyContract;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class QuickAddService extends IntentService {
    // Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_QUICK_ADD =
            "com.example.lonejourneyman.buoynow.action.ACTION_QUCK_ADD";
    public static final String ACTION_DATA_UPDATED =
            "com.example.lonejourneyman.buoynow.action.ACTION_DATA_UPDATED";
    private String TAG = getClass().getSimpleName();

    public QuickAddService() {
        super("QuickAddService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_QUICK_ADD.equals(action)) { handleActionQuickAdd(); }
        }
    }

    private void handleActionQuickAdd() {
        GoogleApiClient mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mApiClient.connect();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mApiClient)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get location.");
                                return;
                            }
                            Location location = locationResult.getLocation();
                            addLocationTask(location);
                        }
                    });
            return;
        }
        mApiClient.disconnect();
    }

    private void addLocationTask(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(BuoyContract.BuoyEntry.COLUMN_DESCRIPTION, getString(R.string.default_desc));
        cv.put(BuoyContract.BuoyEntry.COLUMN_DETAILS, getString(R.string.default_details));
        cv.put(BuoyContract.BuoyEntry.COLUMN_LONG, location.getLongitude());
        cv.put(BuoyContract.BuoyEntry.COLUMN_LAT, location.getLatitude());

        Uri uri = getContentResolver().insert(BuoyContract.BuoyEntry.CONTENT_URI, cv);
        if (uri != null) {
            if (Utility.getSendNotification(this)) {
                Utility.sendNotification(this); }
            Utility.updateWidget(this);
        }
    }
}
