package com.example.lonejourneyman.buoynow.widget;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.lonejourneyman.buoynow.R;
import com.example.lonejourneyman.buoynow.data.BuoyContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class QuickListService extends RemoteViewsService {

    public final String TAG = QuickListService.class.getSimpleName();

    static final int INDEX_BUOY_ID = 0;
    static final int INDEX_BUOY_DESC = 1;
    static final int INDEX_BUOY_DETAIL = 2;
    static final int INDEX_BUOY_LONG = 3;
    static final int INDEX_BUOY_LAT = 4;
    static final int INDEX_BUOY_TIMESTAMP = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                Uri buoyListUri = BuoyContract.BuoyEntry.CONTENT_URI;
                data = getContentResolver().query(buoyListUri,
                        null,
                        null,
                        null,
                        BuoyContract.BuoyEntry.COLUMN_TIMESTAMP + " DESC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.quick_list_detail_widget);
                String buoyDate = data.getString(INDEX_BUOY_TIMESTAMP);

                SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date myDate = inputSDF.parse(buoyDate);
                    SimpleDateFormat outputSDF = new SimpleDateFormat("EEEE  MMM dd, yyyy  HH:mm a");
                    buoyDate = outputSDF.format(myDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String buoyDescription = data.getString(INDEX_BUOY_DESC);
                String buoyLong = data.getString(INDEX_BUOY_LONG);
                String buoyLat = data.getString(INDEX_BUOY_LAT);
                String buoyLongLat = getString(R.string.list_lat_short) + buoyLat +
                        getString(R.string.pad_widget) + getString(R.string.list_long_short) +
                        buoyLong;

                views.setTextViewText(R.id.widget_buoy_date, buoyDate);
                views.setTextViewText(R.id.widget_buoy_description, buoyDescription);
                views.setTextViewText(R.id.widget_buoy_longlat, buoyLongLat);

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("DatabaseIndex", data.getString(INDEX_BUOY_ID));
                fillInIntent.putExtra("Description", buoyDescription);
                fillInIntent.putExtra("DatabaseDetails", data.getString(INDEX_BUOY_DETAIL));
                fillInIntent.putExtra("Longitude", buoyLong);
                fillInIntent.putExtra("Latitude", buoyLat);
                fillInIntent.putExtra("Date", buoyDate);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.quick_list_detail_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) return data.getLong(INDEX_BUOY_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
