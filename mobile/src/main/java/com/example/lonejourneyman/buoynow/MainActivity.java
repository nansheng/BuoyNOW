package com.example.lonejourneyman.buoynow;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lonejourneyman.buoynow.data.BuoyContract;
import com.example.lonejourneyman.buoynow.settings.BuoySettingsActivity;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        LoaderManager.LoaderCallbacks<Cursor>{

    private String TAG = getClass().getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "com.example.lonejourneyman.buoynow.action.ACTION_DATA_UPDATED";
    public static final String AUTO_CONVERSATION_NAME = "buoy now";
    private static final int TASK_LOADER_ID = 0;
    private static final int TASK_SEARCH_ID = 1;
    private static final int MY_PERMISSION_LOCATION = 1;
    private static final int AUTO_MANAGE = 1;
    private static GoogleApiClient mApiClient;
    private BuoyListAdapter mAdapter;
    private String searchQuery;

    Boolean initialCountdown = true;
    CountDownTimer triggerCountdown;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.all_buoy_list_view) RecyclerView mRecyclerView;
    @BindView(R.id.empty_list_view) TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (savedInstanceState != null && savedInstanceState.containsKey("Query")) {
            searchBuoys(savedInstanceState.getString("Query")); }

        if (checkAndRequestLocationPermissions()) {}

        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new BuoyListAdapter(context);
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = BuoyContract.BuoyEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                // Delete in Database
                getContentResolver().delete(uri, null, null);
                // Trigger the Load of List
                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null,MainActivity.this);
                Utility.updateWidget(MainActivity.this);
            }
        }).attachToRecyclerView(mRecyclerView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AwarenessQueryTask().execute();
            }
        });

        mApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Awareness.API)
                .enableAutoManage(this, AUTO_MANAGE, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "Google API Client is connected");
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "Google API Client is suspended!");
                    }
                })
                .build();

        if (((BuoyNowApplication)this.getApplication()).getInitialRun() &&
                Utility.getInitialAutoSave(this)) {
            ((BuoyNowApplication)this.getApplication()).setInitialRun(false);
            new AwarenessQueryTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBuoys(query);
                searchView.clearFocus();
                //searchView.setQuery("", false);
                //searchView.setIconified(true);
                searchItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(final String newText) {
                if (!initialCountdown) {
                    triggerCountdown.cancel();
                }
                triggerCountdown = new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        searchBuoys(newText);
                        searchQuery = newText;
                    }
                };
                triggerCountdown.start();
                initialCountdown = false;
                return false;
            }
        });
        return true;
    }

    private void searchBuoys(String query) {
        Bundle qBundle = new Bundle();
        qBundle.putString("query",query);

        getSupportLoaderManager().restartLoader(TASK_SEARCH_ID, qBundle, MainActivity.this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (searchQuery != null) outState.putString("Query", searchQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, BuoySettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION);
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION);
            }
            return false;
        } else { return true; }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new AwarenessQueryTask().execute();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.perm_location))
                            .setPositiveButton(MainActivity.this.getResources().getString(
                                    android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSION_LOCATION);}})
                            .setNegativeButton(MainActivity.this.getResources().getString(
                                    android.R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {}}).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) { deliverResult(mTaskData); }
                else { forceLoad(); }
            }

            @Override
            public Cursor loadInBackground() {
                String sortString = (Utility.getListSorting(this.getContext()).equals("0"))
                        ? BuoyContract.BuoyEntry.COLUMN_TIMESTAMP + " DESC" :
                        BuoyContract.BuoyEntry.COLUMN_DESCRIPTION + " ASC";
                String buoySelection = null;
                if (id == 1 && args !=null && args.containsKey("query")) {
                    buoySelection = (args.getString("query").isEmpty()) ? null :
                            BuoyContract.BuoyEntry.COLUMN_DESCRIPTION + " LIKE '%" +
                                    args.getString("query") + "%'";
                }
                try {
                    return getContentResolver().query(BuoyContract.BuoyEntry.CONTENT_URI,
                            null,
                            buoySelection,
                            null,
                            sortString);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load location data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data.getCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public class AwarenessQueryTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
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
            } else { Log.i(TAG, "Not going into getLocation"); }
            return null;
        }
    }

    private void addLocationTask(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(BuoyContract.BuoyEntry.COLUMN_DESCRIPTION, getString(R.string.default_desc));
        cv.put(BuoyContract.BuoyEntry.COLUMN_DETAILS, getString(R.string.default_details));
        cv.put(BuoyContract.BuoyEntry.COLUMN_LONG, location.getLongitude());
        cv.put(BuoyContract.BuoyEntry.COLUMN_LAT, location.getLatitude());
        // insert new Location into Database
        Uri uri = getContentResolver().insert(BuoyContract.BuoyEntry.CONTENT_URI, cv);
        if (uri != null) {
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
            if (Utility.getSendNotification(MainActivity.this)) {
                Utility.sendNotification(MainActivity.this); }
            Utility.updateWidget(MainActivity.this);
         }
    }
}