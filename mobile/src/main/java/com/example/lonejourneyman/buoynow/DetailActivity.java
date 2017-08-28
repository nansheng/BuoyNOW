package com.example.lonejourneyman.buoynow;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lonejourneyman.buoynow.data.BuoyContract;
import com.example.lonejourneyman.buoynow.data.BuoyDBHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = getClass().getSimpleName();
    private GoogleMap mMap;
    BuoyDBHelper mBuoysDbHelper;
    String detailIndex;
    String detailDesc;
    String detailDetails;
    String detailLong;
    String detailLat;
    String[] detailDate;
    LatLng detailLoc;
    SQLiteDatabase db;
    Boolean initialCountdown = true;
    CountDownTimer triggerCountdown;

    @BindView(R.id.buoy_description) EditText editTitle;
    @BindView(R.id.text_description) EditText editDescription;
    @BindView(R.id.text_latitude) TextView detLat;
    @BindView(R.id.text_longitude) TextView detLong;
    @BindView(R.id.text_address) TextView detTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        ((BuoyNowApplication)this.getApplication()).setInitialRun(false);

        mBuoysDbHelper = new BuoyDBHelper(getBaseContext());
        db = mBuoysDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        detailIndex = intent.getStringExtra("DatabaseIndex");
        detailDesc = intent.getStringExtra("Description");
        detailDetails = intent.getStringExtra("DatabaseDetails");
        detailLong = intent.getStringExtra("Longitude");
        detailLat = intent.getStringExtra("Latitude");
        detailDate = intent.getStringExtra("Date").split(" ");

        setTitle(
                detailDate[1] + " " +
                        detailDate[2] + " " +
                        detailDate[3] + " " +
                        detailDate[4] + " " +
                        detailDate[5] + " " +
                        detailDate[6] + " " +
                        detailDate[7]);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editTitle.setText(detailDesc);
        editTitle.addTextChangedListener(new GenericTextWatcher(editTitle));
        editDescription.setText(detailDetails);
        editDescription.addTextChangedListener(new GenericTextWatcher(editDescription));

        Double dLat = Double.valueOf(detailLat);
        Double dLong = Double.valueOf(detailLong);
        detailLoc = new LatLng(dLat, dLong);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geocoder.getFromLocation(dLat, dLong, 1);
            if (address != null && address.size() > 0) {
                String detAddress = address.get(0).getAddressLine(0);
                String detCity = address.get(0).getLocality();
                String detState = address.get(0).getSubAdminArea();
                String detCountry = address.get(0).getCountryName();
                String detPostal = address.get(0).getPostalCode();

                String detLatText = getString(R.string.list_lat) + detailLat + getString(R.string.pad_detail);
                String detLongText = getString(R.string.list_long) + detailLong;
                String detAddText = getString(R.string.list_address) + detAddress;
                detLat.setText(detLatText);
                detLong.setText(detLongText);
                detTitle.setText(detAddText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 0, 0, 0);
        mMap.addMarker(new MarkerOptions()
                .position(detailLoc)
                .title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((detailLoc), 18));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class GenericTextWatcher implements TextWatcher {
        private View watcherView;

        private GenericTextWatcher(View watcherView) {
            this.watcherView = watcherView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(final Editable s) {
            if (!initialCountdown) {
                triggerCountdown.cancel();
            }
            triggerCountdown = new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }
                @Override
                public void onFinish() {
                    ContentValues cv = new ContentValues();
                    switch (watcherView.getId()) {
                        case R.id.buoy_description:
                            cv.put("buoyDescription", s.toString());
                            break;
                        case R.id.text_description:
                            cv.put("buoyDetails", s.toString());
                            break;
                    }
                    if (cv.size() > 0) {
                        Uri uri = BuoyContract.BuoyEntry.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(detailIndex).build();
                        getContentResolver().update(uri, cv,null, null);
                    }
                }
            };
            triggerCountdown.start();
            initialCountdown = false;
        }
    }
}
