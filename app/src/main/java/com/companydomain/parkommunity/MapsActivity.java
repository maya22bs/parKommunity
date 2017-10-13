package com.companydomain.parkommunity;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private String longtitude;
    private String latitude;
    private ArrayList<ParkingSpot> spots;
    final private String EXTRA_LAT = "lat extra";
    final private String EXTRA_LONG = "lot extra";
    private Activity currentActivity = this;
    protected GoogleApiClient mGoogleApiClient;

    protected Location mLastLocation;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    private String currLatDialog;
    private String currLongDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        if (intent != null) {
            longtitude = intent.getStringExtra(EXTRA_LONG);
            latitude = intent.getStringExtra(EXTRA_LAT);
        }

        buildGoogleApiClient();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
       /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLocationCheck();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();

        mGoogleApiClient.connect();*/


       // mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng currLocation = new LatLng(Utility.stringToDouble(latitude), Utility.stringToDouble(longtitude));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation,18));


        for(ParkingSpot spot: GroupDetailActivity.listOfParkingSpots) {
            LatLng tmpspot = new LatLng(Utility.stringToDouble(spot.getLatitude()),Utility.stringToDouble( spot.getLongitude()));
            MarkerOptions marker=new MarkerOptions().position(tmpspot).title(spot.getAddress());
            mMap.addMarker(marker);
            //  mMap.moveCamera(CameraUpdateFactory.newLatLng(tmpspot));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker marker) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View myView = inflater.inflate(R.layout.map_navigate_dialog, null);
                builder.setView(myView);
                LatLng position = marker.getPosition();
                currLongDialog =Utility.doubleToString(position.longitude);
                currLatDialog =Utility.doubleToString(position.latitude);
                builder.show();
                TextView address= (TextView) myView.findViewById(R.id.map_nav_adress_text_view);
                if(address!=null){
                    address.setText(marker.getTitle());
                }
                return true;
            }

        });
    }



    public void openWazeIntent(View view/*double longtitude, double latitude*/){
        try {
            String url = "waze://?ll=" + currLatDialog+","+currLongDialog+"&navigate=yes";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            startActivity(intent);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateCurrentLocation();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLocationCheck();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    private void permissionLocationCheck() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(currentActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            return;
        }
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
