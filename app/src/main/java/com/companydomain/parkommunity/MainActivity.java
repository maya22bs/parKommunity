package com.companydomain.parkommunity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.companydomain.parkommunity.Constants.STATE_FINISHED_UPDATE;
import static com.companydomain.parkommunity.Database.addAskForHelpToDb;
import static com.companydomain.parkommunity.Database.initMyStaticVariables;
import static com.companydomain.parkommunity.Database.isUserAuth;
import static com.companydomain.parkommunity.Database.static_currentState;
import static com.companydomain.parkommunity.Database.static_mFirebaseDatabaseReference;
import static com.companydomain.parkommunity.Database.static_myAuthUser;
import static com.companydomain.parkommunity.Database.static_myGroups;
import static com.companydomain.parkommunity.Database.static_myGroupsIds;
import static com.companydomain.parkommunity.Database.static_myRefs;
import static com.companydomain.parkommunity.Database.static_myRefsState;
import static com.companydomain.parkommunity.Database.static_myUser;
import static com.companydomain.parkommunity.Database.static_otherGroups;
import static com.companydomain.parkommunity.Database.static_uId;
import static com.companydomain.parkommunity.Utility.dpToPixel;
import static com.companydomain.parkommunity.Utility.showToast;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private Activity currentActivity = this;

    private static final String TAG = "TAG_"+ MainActivity.class.getSimpleName();
    private final int RC_FACEBOOK_SIGN_IN = 64206;

    public static SharedPreferences mSharedPreferences;


    //extra string
    private final String NEXT_CLASS_EXTRA="next class extra";

    private Handler mHandler = new Handler();
    Runnable mHandlerTask ;

    private LoginButton mFacebookLoginButton;
    public static CallbackManager mCallbackManager;

    private List<Boolean> myChoices;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);



        //navigation grid
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setLogo(R.drawable.ic_logo_trans);
        menu.setDisplayShowTitleEnabled(false);
        menu.setDisplayUseLogoEnabled(true);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                try{
                    mFacebookLoginButton = (LoginButton)findViewById(R.id.login_button_facebook);

                    TextView nameView=(TextView) findViewById(R.id.name_text_view);
                    //TextView emailView=(TextView) findViewById(R.id.email_text_view);
                    ImageView personImage = (ImageView) findViewById(R.id.profile_picture);

                    //fixing margins and stuff
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

                    int top_px=0;
                    if(!isUserAuth()){
                        nameView.setVisibility(View.GONE);
                        personImage.setVisibility(View.GONE);
                        top_px = dpToPixel(currentActivity, 48);

                        mFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(MainActivity.this, SignInActivity.class);
                                startActivity(intent);
                                onBackPressed();
                            }
                        });

                    }else{
                        nameView.setVisibility(View.VISIBLE);
                        personImage.setVisibility(View.VISIBLE);
                        top_px = dpToPixel(currentActivity, 16);

                        mFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(MainActivity.this, SignInActivity.class);
                                signOut();
                                onBackPressed();
                            }
                        });
                    }

                    params.setMargins(0, top_px, 0, 0);
                    mFacebookLoginButton.setLayoutParams(params);


                    String username="";
                    String email="";
                    Uri pic_url=null;
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                        username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        //email= FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        pic_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
                    }

                    nameView.setText(username);
                    //emailView.setText(email);
                    Picasso.with(MainActivity.this).load(pic_url).error(R.drawable.ic_menu_camera).into(personImage);

                }catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("drawer opening!", "!!!!!!!!!!!");
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        Button notifyFreeParkButton = (Button) findViewById(R.id.notify_free_park_button);
        Button askForHelpButton = (Button) findViewById(R.id.ask_for_help_button);


        //buttons animation
        Animation animUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        Animation animDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        notifyFreeParkButton.startAnimation(animDown);
        askForHelpButton.startAnimation(animUp);


        //buttons onclick
        notifyFreeParkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !isUserAuth()) {
                    // Not signed in, launch the Sign In activity
                    //Intent intent=new Intent(MainActivity.this, SignInActivity.class);
                    //intent.putExtra(NEXT_CLASS_EXTRA, NotifyFreeParkActivty.class);
                     //startActivity(intent);

                    //TODO:
                    showToast(currentActivity, "TODO:  need to log in");

                }
                else{
                    //testNotifyFreeParking();
                    startActivity(new Intent(MainActivity.this, NotifyFreeParkActivty.class));

                }
            }
        });

        askForHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !isUserAuth()) {
                    // Not signed in, launch the Sign In activity

                    //TO DO add a way to notify the signin activity that after the login it should automaticly send the help notification
                    //But it's really not in high priority!!! we wont use a flow where the user clicked this unsigned.
//                    Intent intent=new Intent(MainActivity.this, SignInActivity.class);
//                    intent.putExtra(NEXT_CLASS_EXTRA, MainActivity.class);
//                    startActivity(intent);
                    //TODO:
                    showToast(currentActivity, "TODO:  need to log in");

                }
                else{
                    //TO DO: get curent loction and send help notification to relevant group

//                    Utility.showNotification(MainActivity.this,"Reqular notification", "test test test");
//                    Utility.showNotificationTest(MainActivity.this, "Button notification", "test test test");

//                   showSelectGroupDialog();

                    mHandlerTask = new Runnable() {
                        @Override
                        public void run() {
                            if ( static_myRefsState != STATE_FINISHED_UPDATE) {
                                Log.d(TAG, "adding PS--> waiting");
                                mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                            }else {
                                Log.d(TAG, "adding PS--> success");
                                showSelectGroupDialog2();
                                mHandler.removeCallbacks(mHandlerTask);
                            }
                        }
                    };

                    mHandlerTask.run();

                }
            }
        });




    }


    private void showSelectGroupDialog(){

        final List<Group> groups = Database.static_myGroups;
        //myChoices = new ArrayList<>(groups.size());

        if (groups.size() > 0) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
            builderSingle.setIcon(R.drawable.com_facebook_button_icon);
            builderSingle.setTitle("Select groups");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this,
                    android.R.layout.select_dialog_multichoice);

            for (Group group : groups) {
                arrayAdapter.add(group.getName());
            }

//            builderSingle.setPositiveButton(
//                    "cancel",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Log.d(TAG,"hey: "+which);
//                            onSelectedGropAskForHelp(groups.get(which));
//                        }
//                    });

            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            Group groupInfo = groups.get(which);

                            onSelectedGropAskForHelp(groupInfo);

                            // end of ok dialog
                        }
                    });
            builderSingle.show();
        }else{
            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                    MainActivity.this);
            builderInner.setTitle(R.string.no_groups);
            builderInner.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            dialog.dismiss();
                        }
                    });
            builderInner.show();
        }
    }

    private void showSelectGroupDialog2(){

        final List<Group> groups = Database.static_myGroups;
        myChoices = new ArrayList<>();
        for(int i=0;i<groups.size();i++){
            Log.d(TAG,"here4: "+i);
            myChoices.add(false);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_multichoice);

        for (Group group : groups) {
            arrayAdapter.add(group.getName());
        }

        final AlertDialog dialog = new AlertDialog.Builder(currentActivity)
                .setTitle("Choose groups")
                .setAdapter(arrayAdapter, null)
                .setPositiveButton("Send", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick( DialogInterface dialog, int which) {
                       Log.d(TAG, "clicked ok");
                        for(int i=0;i<groups.size();i++){
                            if (myChoices.get(i)) {
                                onSelectedGropAskForHelp(groups.get(i));
                                Log.d(TAG,"send ask for help to :" + groups.get(i).getName());
                            }
                        }
                    }

                }).create();

        dialog.getListView().setItemsCanFocus(false);
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Manage selected items here
                Log.d(TAG, "clicked" + position);
                CheckedTextView textView = (CheckedTextView) view;
                if(textView.isChecked()) {
                    myChoices.set(position,true);
                } else {
                    myChoices.set(position, false);
                }
            }
        });

        dialog.show();

    }

    private void onSelectedGropAskForHelp(Group group){
        Log.d(TAG, "opened");
        addAskForHelpToDb(this, group);
    }

    private void testNotifyFreeParking(){
        //TODO: put real values
        Random r = new Random();
        String longtitude = String.valueOf(r.nextDouble());
        String latitude = String.valueOf(r.nextDouble());
        String address = "dummy address "+String.valueOf(r.nextDouble());
        String status = "dummy status" + String.valueOf(r.nextDouble());

        DateFormat df = new SimpleDateFormat("dd/MM , HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        final ParkingSpot ps = new ParkingSpot(longtitude, latitude, address, static_myUser.getName(), static_myUser.getPhotoUrl());

        Log.d(TAG, "adding PS");

        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                if ( static_myRefsState != STATE_FINISHED_UPDATE) {
                    Log.d(TAG, "adding PS--> waiting");
                    mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                }else {
                    Log.d(TAG, "adding PS--> success");
                    addParkingSpotToDB(ps);
                    mHandler.removeCallbacks(mHandlerTask);
                }
            }
        };

       mHandlerTask.run();

    }


    private void addParkingSpotToDB(ParkingSpot ps){

        //currently adding only to test group
        for(Map.Entry<String, DatabaseReference> entry : static_myRefs.entrySet()) {
            DatabaseReference myRef = entry.getValue();
            if( entry.getKey().equals("5c442709-c0f1-47db-b8db-1dad7f58249b")) {
                myRef.push().setValue(ps);
            }

        }
    }


    private void createGroupDialog(){
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//
//        alert.setTitle("create group");
//        alert.setMessage("choose group name");
//
//// Set an EditText view to get user input
//        final EditText input = new EditText(this);
//        alert.setView(input);
//
//        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//                String groupId = getUniqueGroupId();
//                String name = input.getText().toString();
//                List<String> usersList = new ArrayList<>();
//                usersList.add(static_uId);
//
//                //TODO:
//                String longtitude ="34.7786484";
//                String latitude = "32.0617584";
//                String radius = "10";
//                final Group group = new Group(groupId , name, usersList, longtitude, latitude, radius);
//                static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_INFO).child(group.getGroupId()).setValue(group);
//
//                Log.d(TAG, "adding group--> waiting");
//
//                mHandlerTask = new Runnable() {
//                    @Override
//                    public void run() {
//                        if ( static_currentState != STATE_FINISHED_UPDATE) {
//                            Log.d(TAG, "adding group--> waiting");
//                            mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
//                        }else {
//                            Log.d(TAG, "adding group--> success");
//                            addGroupToUser(currentActivity, group);
//                            mHandler.removeCallbacks(mHandlerTask);
//                        }
//                    }
//                };
//
//                mHandlerTask.run();
//            }
//        });
//
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // Canceled.
//            }
//        });
//
//        alert.show();

        // change

        startActivity(new Intent(MainActivity.this, CreateGroupActivity.class));


    }

    private void signOut(){
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        //Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        static_mFirebaseDatabaseReference = null;
        static_myAuthUser = null;
        static_myUser = null;
        static_myGroupsIds = null;
        static_myGroups = null;
        static_otherGroups = null;
        static_uId = null;

        //TODO: disableMyRefs();


    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_create_group) {

            if( isUserAuth() ){
                createGroupDialog();
            }else{
                //TODO: toast?
                showToast(currentActivity,"not logged in");
            }

        }else if (id == R.id.nav_groups_list) {

            if( isUserAuth() ){
                Log.d(TAG, "view groups");

                mHandlerTask = new Runnable() {
                    @Override
                    public void run() {
                        if ( static_currentState != STATE_FINISHED_UPDATE) {
                            Log.d(TAG, "view groups--> waiting");
                            mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                        }else {
                            Log.d(TAG, "view groups--> success");
                            startActivity(new Intent(currentActivity, GroupsListActivity.class));
                            mHandler.removeCallbacks(mHandlerTask);
                        }
                    }
                };

                mHandlerTask.run();
            }else{
                //TODO: toast?
                showToast(currentActivity,"not logged in");
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onResume(){
        super.onResume();
        AppEventsLogger.activateApp(this);
        Log.d(TAG,"on resume here");
        if(isUserAuth()){
            Log.d(TAG,"on resume here2");
            initMyStaticVariables(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void showCreateGroupDialog() {

    }


}
