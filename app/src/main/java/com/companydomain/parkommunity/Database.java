package com.companydomain.parkommunity;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.companydomain.parkommunity.Constants.DB_ASK;
import static com.companydomain.parkommunity.Constants.DB_GROUPS;
import static com.companydomain.parkommunity.Constants.DB_INFO;
import static com.companydomain.parkommunity.Constants.DB_PS;
import static com.companydomain.parkommunity.Constants.DB_USERS;
import static com.companydomain.parkommunity.Constants.SP_IS_USER_INIT_IN_DB;
import static com.companydomain.parkommunity.Constants.STATE_FINISHED_UPDATE;
import static com.companydomain.parkommunity.Constants.STATE_NO_STATE;
import static com.companydomain.parkommunity.Constants.STATE_UPDATING;
import static com.companydomain.parkommunity.MainActivity.mSharedPreferences;
import static com.companydomain.parkommunity.Utility.savePrefs;
import static com.companydomain.parkommunity.Utility.showNotificationAskForHelp;
import static com.companydomain.parkommunity.Utility.showNotifyNotificationTest;

/**
 * Created by ori on 29/08/16.
 */

public class Database {

    private static final String TAG = "TAG_"+ Database.class.getSimpleName();

    public static int static_currentState = STATE_NO_STATE;
    public static int static_myRefsState = STATE_NO_STATE;
    public static FirebaseUser static_myAuthUser;
    public static User static_myUser;
    public static String static_uId;
    public static List<Group> static_myGroups;
    public static List<String> static_myGroupsIds;
    public static List<Group> static_otherGroups;
    private static int static_counter = 0;
    public static DatabaseReference static_mFirebaseDatabaseReference;
    //public static FirebaseDatabase static_daDatabase;
    public static Map<String,DatabaseReference> static_myRefs; //key: ref title, value: ref
    public static Map<String,DatabaseReference> static_myAskRefs; //key: ref title, value: ref

    public static boolean didIfinished = false; //getting the users of group
    public static ArrayList<User> usersList; //users of group


    public static boolean isUserAuth(){
        static_myAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        return (static_myAuthUser != null);
    }



    public static void initMyStaticVariables(Context context){

        if(static_currentState == STATE_UPDATING){
            return;
        }
        static_currentState = STATE_UPDATING;
        //static_daDatabase = FirebaseDatabase.getInstance();
        static_mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        static_myGroups = new ArrayList<>();
        static_myGroupsIds = new ArrayList<>();
        static_otherGroups = new ArrayList<>();

        //get a user
        static_myAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        static_uId = static_myAuthUser.getUid();
        static_myUser = new User(static_myAuthUser.getUid(), static_myAuthUser.getDisplayName(), static_myAuthUser.getPhotoUrl().toString(), static_myAuthUser.getEmail(), static_myGroupsIds);

        if (!mSharedPreferences.getBoolean(SP_IS_USER_INIT_IN_DB, false) ) {

            static_mFirebaseDatabaseReference.child(DB_USERS).child(static_myUser.getUid()).setValue(static_myUser);
            savePrefs(context, SP_IS_USER_INIT_IN_DB , true);
            getOtherGroups(context);

        }else {
            getMyGroupsIds(context);
        }
    }

    private static void getMyGroupsIds(final Context context){
        static_mFirebaseDatabaseReference.child(DB_USERS).child(static_uId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value

                        static_myUser = dataSnapshot.getValue(User.class);
                        Log.d(TAG, static_myUser.getName());
                        if (static_myUser.getMyGroups() != null) {
                            static_myGroupsIds = static_myUser.getMyGroups();
                        }
                        // Log.d("tag_", static_myGroupsIds.get(0));
                        getMyGroups(context);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    private static void getMyGroups(final Context context){

        static_counter=0;
        if (static_myGroupsIds.size() == 0){
            getOtherGroups(context);
            return;
        }

        for (String gId : static_myGroupsIds){

            static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_INFO).child(gId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            static_counter++;
                            // Get user value
                            Group group = dataSnapshot.getValue(Group.class);
                            static_myGroups.add(group);
                            Log.d(TAG, static_counter + " , "+ group.getName());
                            if( static_counter == static_myGroupsIds.size()){
                                getOtherGroups(context);
                                initMyRefs(context);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

        }

    }

    private static void getOtherGroups(final Context context){
        static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_INFO).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            Group group = child.getValue(Group.class);

                            if(group!=null) {
                                if (!static_myGroupsIds.contains(group.getGroupId())) {
                                    static_otherGroups.add(group);
                                } else {
                                    Log.d(TAG, "didnt added group: " + group.getGroupId());
                                }
                            }
                        }
                        static_currentState = STATE_FINISHED_UPDATE;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    public static void addAskForHelpToDb(Context context, Group group){

        if(static_myAskRefs == null) return;
        group.setWhoGeneratedAskForHelpID(static_uId);
        group.setWhoGeneratedAskForHelpUserName(static_myUser.getName());
        group.setAskForHelpGroupName(group.getName());
        for(Map.Entry<String, DatabaseReference> entry : static_myAskRefs.entrySet()) {
            if (entry.getKey().equals(group.getGroupId())) {
                DatabaseReference myRef = entry.getValue();
                myRef.push().setValue(group);
            }else{
                Log.d(TAG, "group id: " +group.getGroupId() +" key = " + entry.getKey());
            }
        }
    }

    public static void addUserToGroup(Context context,Group group){
        List<String> users = group.getUsers();
        users.add(static_uId);
        group.setUsers(users);

        static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_INFO).child(group.getGroupId()).setValue(group);

        //currentGroup = group;
        addGroupToUser(context , group);

    }

    public static void addGroupToUser(Context context, Group group){

        if (static_myGroupsIds ==null ){
            static_myGroupsIds = new ArrayList<String>();
            static_myGroupsIds.add(group.getGroupId());
            static_myUser.setMyGroups(static_myGroupsIds);
        }else{
            static_myGroupsIds.add(group.getGroupId());
            static_myUser.setMyGroups(static_myGroupsIds);
        }

        if(static_myGroups == null){
            static_myGroups = new ArrayList<>();
        }
        static_myGroups.add(group);

        for (Group g : static_otherGroups){
            if (g.getGroupId().equals(group.getGroupId())){
                static_otherGroups.remove(g);
                break;
            }
        }

        addUserToDb(context);
        //initMyRefs(context);
        addGroupRefToUser(context, group);
    }

    private static void addUserToDb(Context context){
        static_mFirebaseDatabaseReference.child(DB_USERS).child(static_uId).setValue(static_myUser);
    }


    private static void initMyRefs(Context context){

        if(static_myRefsState != STATE_NO_STATE){
            return;
        }
        static_myRefsState = STATE_UPDATING;
        //static_daDatabase = FirebaseDatabase.getInstance();
        //savePrefs(this, SP_MY_REFS_TITLES, new HashSet<String>()); to initialize the set

        //myRefsTitles = mSharedPreferences.getStringSet(SP_MY_REFS_TITLES, new HashSet<String>());
        static_myRefs = new HashMap<>();
        static_myAskRefs = new HashMap<>();

        for (String key : static_myGroupsIds){
            static_myRefs.put(key, static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_PS).child(key));
            static_myAskRefs.put(key, static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_ASK).child(key));
            Log.d(TAG, "ref_title: "+ key);
        }
        addListenersForMyRefs(context);
    }

    private static void addGroupRefToUser(final Context context, Group group){
        String key = group.getGroupId();
        DatabaseReference myRef = static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_PS).child(key);
        DatabaseReference myAskRef = static_mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_ASK).child(key);

        static_myRefs.put(key, myRef);
        static_myAskRefs.put(key, myAskRef);

        addListenersForGroupRef(context, myRef);
        addListenersForAskRef(context, myAskRef);
    }

    private static void addListenersForGroupRef(final Context context, DatabaseReference ref){

        //// only one ref!!!!!


        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onchild ADDED , previos: " + s + "  current: " + dataSnapshot.getKey());
                ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
                Log.d(TAG, "address: " + value.getAddress());
                if (!value.getWhoGeneratedMe().equals(static_myUser.getName()) ) {
                    Log.d(TAG, "you didnt generated this. you:" + static_myUser.getName() +"  value: "+ value.getWhoGeneratedMe());
                    //showNotification(context, "added child", "address: " + value.getAddress());
                    String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
                    showNotifyNotificationTest(context, value);
                }else{
                    Log.d(TAG,"you generated this");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onchild CHANGED , previos: " + s + "  current: " + dataSnapshot.getKey());
//                    ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
//                    Log.d(TAG, "address: " + value.getAddress());
//                    if (!isAppInForeground(context)) {
//                        //showNotification(context, "added child", "address: " + value.getAddress());
//                        String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
//                        showNotifyNotificationTest(context, value);
//                    }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onchild REMOVED ");
//                    ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
//                    Log.d(TAG, "address: " + value.getAddress());
//                    if (!isAppInForeground(context)) {
//                        //showNotification(context, "added child", "address: " + value.getAddress());
//                        String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
//                        showNotifyNotificationTest(context, value);
//                    }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onchild MOVED : " + s);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "on CANCELLED");

            }
        });
    }



    private static void addListenersForAskRef(final Context context , DatabaseReference ref){

        ///// only one ref!!!!!

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.d(TAG, "onchild ADDED askforhelp , previos: " + s + "  current: " + dataSnapshot.getKey());

                // Log.d(TAG, "onchild ADDED , previos: " + s + "  current: " + dataSnapshot.getKey());
//                    Log.d(TAG, "helllo");
                Log.d(TAG, "why here");
                Group value = dataSnapshot.getValue(Group.class);
                if(value.getWhoGeneratedAskForHelpID() == null){
                    Log.d(TAG, "why yes");
                }else{
                    Log.d(TAG, "why no: "+value.getWhoGeneratedAskForHelpUserName());
                }
                //Log.d(TAG, "address: " + value.getAddress());
                if ( !value.getWhoGeneratedAskForHelpID().equals(static_uId) ) {

                    showNotificationAskForHelp(context, value);
                }else{
                    Log.d(TAG, "didnt shiw nitification");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Log.d(TAG, "onchild CHANGED , previos: " + s + "  current: " + dataSnapshot.getKey());
                Group value = dataSnapshot.getValue(Group.class);
                //Log.d(TAG, "address: " + value.getAddress());

                if ( value.getWhoGeneratedAskForHelpID() != static_uId) {

                    showNotificationAskForHelp(context, value);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onchild REMOVED ");
//                    Group value = dataSnapshot.getValue(Group.class);
//                   // Log.d(TAG, "address: " + value.getAddress());
//
//                    if (!isAppInForeground(context) && value.getWhoGeneratedAskForHelpID() != static_uId) {
//
//                        showNotificationAskForHelp(context, value);
//                    }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onchild MOVED : " + s);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "on CANCELLED");

            }
        });
    }

    private static void addListenersForMyRefs(final Context context) {

        for (Map.Entry<String, DatabaseReference> entry : static_myRefs.entrySet()) {
            //String myRefTitle = entry.getKey();
            DatabaseReference myRef = entry.getValue();

            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onchild ADDED , previos: " + s + "  current: " + dataSnapshot.getKey());
                    ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
                    Log.d(TAG, "address: " + value.getAddress());
                    if (!value.getWhoGeneratedMe().equals(static_myUser.getName()) ) {
                        Log.d(TAG, "you didnt generated this. you:" + static_myUser.getName() +"  value: "+ value.getWhoGeneratedMe());
                        //showNotification(context, "added child", "address: " + value.getAddress());
                        String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
                        showNotifyNotificationTest(context, value);
                    }else{
                        Log.d(TAG,"you generated this");
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onchild CHANGED , previos: " + s + "  current: " + dataSnapshot.getKey());
//                    ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
//                    Log.d(TAG, "address: " + value.getAddress());
//                    if (!isAppInForeground(context)) {
//                        //showNotification(context, "added child", "address: " + value.getAddress());
//                        String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
//                        showNotifyNotificationTest(context, value);
//                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onchild REMOVED ");
//                    ParkingSpot value = dataSnapshot.getValue(ParkingSpot.class);
//                    Log.d(TAG, "address: " + value.getAddress());
//                    if (!isAppInForeground(context)) {
//                        //showNotification(context, "added child", "address: " + value.getAddress());
//                        String longlatString="geo: "+ value.getLatitude()+","+value.getLongitude();
//                        showNotifyNotificationTest(context, value);
//                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onchild MOVED : " + s);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "on CANCELLED");

                }
            });
        }

        for (Map.Entry<String, DatabaseReference> entry : static_myAskRefs.entrySet()) {
//            Log.d(TAG, "iamhehre");
            //String myRefTitle = entry.getKey();
            DatabaseReference myRef = entry.getValue();

            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Log.d(TAG, "onchild ADDED askforhelp , previos: " + s + "  current: " + dataSnapshot.getKey());

                   // Log.d(TAG, "onchild ADDED , previos: " + s + "  current: " + dataSnapshot.getKey());
//                    Log.d(TAG, "helllo");
                    Log.d(TAG, "why here");
                    Group value = dataSnapshot.getValue(Group.class);
                    if(value.getWhoGeneratedAskForHelpID() == null){
                        Log.d(TAG, "why yes");
                    }else{
                        Log.d(TAG, "why no: "+value.getWhoGeneratedAskForHelpUserName());
                    }
                    //Log.d(TAG, "address: " + value.getAddress());
                    if ( !value.getWhoGeneratedAskForHelpID().equals(static_uId) ) {

                        showNotificationAskForHelp(context, value);
                    }else{
                        Log.d(TAG, "didnt shiw nitification");
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //Log.d(TAG, "onchild CHANGED , previos: " + s + "  current: " + dataSnapshot.getKey());
                    Group value = dataSnapshot.getValue(Group.class);
                    //Log.d(TAG, "address: " + value.getAddress());

                    if ( value.getWhoGeneratedAskForHelpID() != static_uId) {

                        showNotificationAskForHelp(context, value);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onchild REMOVED ");
//                    Group value = dataSnapshot.getValue(Group.class);
//                   // Log.d(TAG, "address: " + value.getAddress());
//
//                    if (!isAppInForeground(context) && value.getWhoGeneratedAskForHelpID() != static_uId) {
//
//                        showNotificationAskForHelp(context, value);
//                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onchild MOVED : " + s);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "on CANCELLED");

                }
            });
        }

        static_myRefsState = STATE_FINISHED_UPDATE;

    }


    public static void getUsersListFromGroup(Group group){

        didIfinished = false;
        usersList = new ArrayList<>();

        static_counter=0;
        final int maxSize = group.getUsers().size();
        if (maxSize == 0){
            didIfinished = true;
            return;
        }

        for(String uId : group.getUsers()){

            static_mFirebaseDatabaseReference.child(DB_USERS).child(uId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            static_counter++;
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            usersList.add(user);

                            if( static_counter == maxSize){
                                didIfinished = true;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

        }

    }


}
