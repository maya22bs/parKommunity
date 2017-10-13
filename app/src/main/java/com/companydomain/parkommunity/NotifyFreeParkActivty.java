package com.companydomain.parkommunity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Map;

import static com.companydomain.parkommunity.Constants.STATE_FINISHED_UPDATE;
import static com.companydomain.parkommunity.Database.static_myRefs;
import static com.companydomain.parkommunity.Database.static_myRefsState;
import static com.companydomain.parkommunity.Database.static_myUser;




public class NotifyFreeParkActivty extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {


    public static Location static_location = null;

    private static final String TAG = "TAG_" + NotifyFreeParkActivty.class.getSimpleName();
    private static final long LOCATION_REQUEST_INTERVAL_FREQ = 10000;
    private static final long LOCATION_REQUEST_FAST_INTERVAL_FREQ = 5000;
    private Activity currentActivity = this;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Google play services entry point
    protected GoogleApiClient mGoogleApiClient;

    // Last geographical location
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;

    protected boolean mAddressRequested;
    protected String mAddressOutput;

    private AddressResultReceiver mResultReceiver;

    // view elements
    protected EditText mLocationAddressEditText;
    protected Button mFetchAddressButton;

    // place fragment
    PlaceAutocompleteFragment mAddressNameFragment;

    // selection value
    protected LatLng mFinalLatLng;
    protected String mFinalAddressOutput;

    private String userName;

    private Handler mHandler = new Handler();
    Runnable mHandlerTask ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_free_park);

        mResultReceiver = new AddressResultReceiver(new Handler());

        mAddressNameFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        //Address achieve services
//        mLocationAddressEditText = (EditText) findViewById(R.id.insert_address_edit_text);
        mFetchAddressButton = (Button) findViewById(R.id.button);

        mAddressRequested = false;
        mAddressOutput = "";

        buildGoogleApiClient();
        setAutocompleteFragment();
        createLocationRequest();
        updateUIWidgets();
        fetchAddressButtonHandler(null);
        android.app.Fragment placeAutocompleteFragment=getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        ((EditText)placeAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(13.0f);
       // ((EditText)placeAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("SEARCH ADDRESS");
        ((EditText)placeAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setPadding(0,0,0,0);



        userName= FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Button sendButton =(Button) findViewById(R.id.send_notify_free_park_button) ;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAddressNameFragment == null){
                    return;
                }

//                EditText addressEditText=(EditText) findViewById(R.id.insert_address_edit_text);
//                final String address=addressEditText.getText().toString();

                final String address = mFinalAddressOutput;
                //String longlatString="geo: "+ mFinalLatLng.latitude+","+mFinalLatLng.longitude;
                //Log.d("FinalLatLng.latitude ", mFinalLatLng.latitude+"");
                //Log.d("FinalLatLng.longitude ", mFinalLatLng.longitude+"");
                ArrayList<Group> groupsResult=getGroupsForLocation(mFinalLatLng.latitude,mFinalLatLng.longitude);



                ParkingSpot spot=new ParkingSpot(Utility.doubleToString(mFinalLatLng.longitude),Utility.doubleToString(mFinalLatLng.latitude),address,static_myUser.getName(), static_myUser.getPhotoUrl());

                //Utility.showNotifyNotificationTest(NotifyFreeParkActivty.this, "Free Park!",
                       // userName + " notified free park at "+ address , longlatString,address);

                notifyFreeParking(groupsResult,spot);
                //BLA BLA BLA
                finish();
            }
        });



    }

    private void notifyFreeParking(final ArrayList<Group> groups , final ParkingSpot ps){

        Log.d(TAG, "adding PS");

        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                if ( static_myRefsState != STATE_FINISHED_UPDATE) {
                    Log.d(TAG, "adding PS--> waiting");
                    mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                }else {
                    Log.d(TAG, "adding PS--> success");
                    for (Group group : groups){
                        addParkingSpotToDB(group.getGroupId(), ps);
                    }
                    mHandler.removeCallbacks(mHandlerTask);
                }
            }
        };

        mHandlerTask.run();
    }

    private void addParkingSpotToDB(String gId , ParkingSpot ps){

        //currently adding only to test group
        for(Map.Entry<String, DatabaseReference> entry : static_myRefs.entrySet()) {
            DatabaseReference myRef = entry.getValue();
            if( entry.getKey().equals(gId)) {
                myRef.push().setValue(ps);
            }

        }
    }

    private ArrayList<Group> getGroupsForLocation(double latitude,double longitude){
        Location groupCenter=new Location("group center");
        Location userCenter=new Location("user center");
        userCenter.setLatitude(latitude);
        userCenter.setLongitude(longitude);

        ArrayList<Group> groupsInPlace=new ArrayList<Group>();
        for(int i=0;i<Database.static_myGroups.size();i++){
            Group currGroup=Database.static_myGroups.get(i);
            if(currGroup.getLatitude()!=null &&currGroup.getLongtitude() !=null) {
                groupCenter.setLatitude(Utility.stringToDouble(currGroup.getLatitude()));
                groupCenter.setLongitude(Utility.stringToDouble(currGroup.getLongtitude()));
                double dis = Math.sqrt(
                        (userCenter.getLatitude() - groupCenter.getLatitude()) * (userCenter.getLatitude() - groupCenter.getLatitude()) +
                                (userCenter.getLongitude() - groupCenter.getLongitude()) * (userCenter.getLongitude() - groupCenter.getLongitude()));
                boolean isInRadius = dis < Utility.stringToDouble(currGroup.getRadius());
                if (isInRadius) {
                    groupsInPlace.add(currGroup);
                }
            }

        }
        return groupsInPlace;
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notify_free_park_activty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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


    private void setAutocompleteFragment() {
        mAddressNameFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                setFinalLocation(place.getLatLng(), place.getAddress().toString());
                updateUIWidgets();
                displayAddressOutput();
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void setFinalLocation(LatLng latLng, String addressOutput) {
        Log.d(TAG, "final location set");
        mFinalLatLng = latLng;
        mFinalAddressOutput = addressOutput;
        mAddressOutput = mFinalAddressOutput;
    }

    public void setFinalLocation(Location location, String addressOutput){
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            setFinalLocation(latLng, addressOutput);
        }
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    public void fetchAddressButtonHandler(View view) {
        // We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        mAddressRequested = true;
        updateUIWidgets();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateCurrentLocation();
        if (mLastLocation != null) {
            setFinalLocation(mLastLocation, "");
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }

            // start location update for father location change. or, new gps data for current location
            startLocationUpdates();
        }
    }


    private void updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLocationCheck();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    private void permissionLocationCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(currentActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            return;
        }
    }



    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
//        mLocationAddressEditText.setText(mAddressOutput);
        if (mAddressNameFragment != null&& mAddressOutput != null ) {
            mAddressNameFragment.setText(mAddressOutput);
        }
    }

    private void updateUIWidgets() {
        if (mAddressRequested) {
            mFetchAddressButton.setEnabled(false);
        } else {
            mFetchAddressButton.setEnabled(true);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        static_location = location;
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL_FREQ);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FAST_INTERVAL_FREQ);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
    }


    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLocationCheck();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, getString(R.string.address_found));
                setFinalLocation(mLastLocation, mAddressOutput);
                displayAddressOutput();
            } else {
//                showToast(getString(R.string.no_address_found));
                Utility.showToast(getBaseContext(), getString(R.string.no_address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
            updateUIWidgets();
        }

    }


    protected void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}
