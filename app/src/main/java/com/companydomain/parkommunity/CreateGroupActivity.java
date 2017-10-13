package com.companydomain.parkommunity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static com.companydomain.parkommunity.Constants.DB_GROUPS;
import static com.companydomain.parkommunity.Constants.DB_INFO;
import static com.companydomain.parkommunity.Constants.STATE_FINISHED_UPDATE;
import static com.companydomain.parkommunity.Database.addGroupToUser;
import static com.companydomain.parkommunity.Database.static_currentState;
import static com.companydomain.parkommunity.Database.static_mFirebaseDatabaseReference;
import static com.companydomain.parkommunity.Database.static_uId;
import static com.companydomain.parkommunity.Utility.getUniqueGroupId;

public class CreateGroupActivity extends Activity implements OnMapReadyCallback {

    private static final long LOCATION_REQUEST_INTERVAL_FREQ = 10000;
    private static final long LOCATION_REQUEST_FAST_INTERVAL_FREQ = 5000;

    public static final String TAG = "TAG_" + CreateGroupActivity.class.getName();

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private EditText mGroupNameEditText;
    private Button mCreateGroupButton;
    protected LatLng mLatLng;

    private MapFragment mapFragment;
    private GoogleMap mMap;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;


    protected LocationRequest mLocationRequest;


    // place fragment
    PlaceAutocompleteFragment mAddressNameFragment;

    private Handler mHandler = new Handler();
    Runnable mHandlerTask;
    private Activity currentActivity = this;


    Circle myCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAddressNameFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_choose_center);

        mGroupNameEditText = (EditText) findViewById(R.id.choose_group_name_editText);
        mCreateGroupButton = (Button) findViewById(R.id.create_gruop_button);

        setAutocompleteFragment();
        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                LatLng center = mMap.getCameraPosition().target;
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                double dRadius = calculateRadiusFromBoundds(bounds);
                String groupId = getUniqueGroupId();
                String name = mGroupNameEditText.getText().toString();
                List<String> usersList = new ArrayList<>();
                usersList.add(static_uId);
                String latitude = center.latitude + "";
                String longitude = center.longitude + "";
                String radius = Double.toString(dRadius);

                final Group group = new Group(groupId, name, usersList, longitude, latitude, radius);
                //TODO insert group to database --> DONE
                Log.d(TAG, "name: " + group.getName() + " lat: " + group.getLatitude() + " lot: " + group.getLongtitude());

                static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_INFO).child(group.getGroupId()).setValue(group);

                Log.d(TAG, "adding group--> waiting");

                mHandlerTask = new Runnable() {
                    @Override
                    public void run() {
                        if (static_currentState != STATE_FINISHED_UPDATE) {
                            Log.d(TAG, "adding group--> waiting");
                            mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                        } else {
                            Log.d(TAG, "adding group--> success");
                            addGroupToUser(currentActivity, group);
                            mHandler.removeCallbacks(mHandlerTask);
                        }
                    }
                };

                mHandlerTask.run();

                finish();
            }

        });

        // Map


        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private double calculateRadiusFromBoundds(LatLngBounds bounds) {
        double x1 = bounds.southwest.latitude;
        double y1 = bounds.southwest.longitude;
        double x2 = bounds.northeast.latitude;
        double y2 = bounds.northeast.longitude;

        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return distance / 2;
    }

    private void setAutocompleteFragment() {
        mAddressNameFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                Log.d(TAG, "select place " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Utility.showToast(getApplicationContext(), getString(R.string.no_address_found));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

//        mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(0, 0))
//                .title("Marker"));


        if (NotifyFreeParkActivty.static_location != null) {
            Location location = NotifyFreeParkActivty.static_location;


            LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 18));

            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            Log.d(TAG, "bounds to string" + bounds.toString());

        }

    }


    // change


}
