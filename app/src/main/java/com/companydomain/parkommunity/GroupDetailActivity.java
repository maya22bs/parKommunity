package com.companydomain.parkommunity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.companydomain.parkommunity.Constants.DB_GROUPS;
import static com.companydomain.parkommunity.Constants.DB_PS;


public class GroupDetailActivity extends Activity {


    public static final String TAG = "TAG_"+GroupDetailActivity.class.getName();

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public String latitude;
        public String longtitude;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    private Activity currentActivity = this;
    public static final String EXTRA_COLOR = "EXTRA_COLOR";
    public static final String EXTRA_CURVE = "EXTRA_CURVE";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static boolean userEnrolled = false;
    public static Group currentGroup;

    private TextView enroll;

    private Button showGroupUserButton;

    final private String EXTRA_LAT="lat extra";
    final private String EXTRA_LONG="lot extra";

    private Button notifyButton;
    private Button askForHelpButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<ParkingSpot, MessageViewHolder> mFirebaseAdapter;
   // private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference myDatabaseReference;
    private FirebaseDatabase myDatabase;



    public static ArrayList<ParkingSpot> listOfParkingSpots = new ArrayList<>();

    final private String EXTRA_SPOTS="spots extra";

    private String currLatDialog;
    private String currLongDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);



        String offset ="";
        for(int i=0;i<15 - currentGroup.getName().length();i++){
            offset.concat(" ");
        }

        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout)).setTitle((offset+currentGroup.getName()));

        //setTitle(offset+currentGroup.getName());
        // tint the circle to match the launching activity
        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        avatar.setBackgroundTintList(
                ColorStateList.valueOf(getIntent().getIntExtra(EXTRA_COLOR, 0xffff00ff)));

        // check if we should used curved motion and load an appropriate transition
        boolean curve = getIntent().getBooleanExtra(EXTRA_CURVE, false);
        getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                .inflateTransition(curve ? R.transition.curve : R.transition.move));




        showGroupUserButton = (Button)findViewById(R.id.show_frind_list_button);
        setShowUserListButton();

        //new stuff

        //notifyButton = (Button) findViewById(R.id.notify_free_park_button);
        //askForHelpButton = (Button) findViewById(R.id.ask_for_help_button);

        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        //mLinearLayoutManager.setStackFromEnd(true);
        mLinearLayoutManager.setReverseLayout(true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<ParkingSpot, MessageViewHolder>(
                ParkingSpot.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(DB_GROUPS).child(DB_PS).child(currentGroup.getGroupId())) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, final ParkingSpot parkingSpot, int position) {
               // mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                listOfParkingSpots.add(parkingSpot);
                Log.d("TAG", "added ps: "+parkingSpot.getAddress());
                viewHolder.messageTextView.setText(Utility.parsedAddress(parkingSpot.getAddress()));
               // viewHolder.messageTextView.setTextSize(20);
                viewHolder.messengerTextView.setText("by "+parkingSpot.getWhoGeneratedMe());
                //viewHolder.messageTextView.setTextSize(18);

               // viewHolder.messengerImageView.setPadding(7,7,7,7);
               // if (friendlyMessage.getPhotoUrl() == null) {
                //    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                 //           R.drawable.ic_account_circle_black_36dp));
                //} else {
                    Glide.with(currentActivity)
                            .load(parkingSpot.getWhoGeneratedMePhoto())
                            .into(viewHolder.messengerImageView);
                //}
                viewHolder.messageTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currLatDialog=parkingSpot.getLatitude();
                        currLongDialog=parkingSpot.getLongitude();
                        showDialog(GroupDetailActivity.this);
                    }

                    });
            }
        };

        android.support.design.widget.CollapsingToolbarLayout appBar = (android.support.design.widget.CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);



        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }else{
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
                //Log.d(TAG, "im here");
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        //floating button map
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Buttons)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupDetailActivity.this, MapsActivity.class);
                intent.putExtra(EXTRA_LAT, currentGroup.getLatitude());
                intent.putExtra(EXTRA_LONG, currentGroup.getLongtitude());
                startActivity(intent);
              //  Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();
            }
        });
    }


    private void setShowUserListButton() {
        showGroupUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(GroupDetailActivity.this, ShowGroupUserListActivity.class);
                //startActivity(intent);
                startActivity(new Intent(GroupDetailActivity.this, ShowGroupUserListActivity.class),
                        ActivityOptions.makeSceneTransitionAnimation(GroupDetailActivity.this
                                , v, "hero"
                        ).toBundle());
            }
        });
    }

    public void showDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.nav_map_dialog, null));

       // builder.setMessage("Choose Action");
        /*builder.setNegativeButton("NAVIGATE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setPositiveButton("SHOW ON MAP", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });*/


        builder.show();
        /*findViewById(R.id.show_on_map_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapIntent(Utility.stringToDouble(longtitude),Utility.stringToDouble(latitude));
            }
        });

        findViewById(R.id.navigate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWazeIntent(Utility.stringToDouble(longtitude),Utility.stringToDouble(latitude));
            }
        });*/
    }

    public void openMapIntent(View view/*double longtitude, double latitude*/){
        String longlatString="geo: "+ currLatDialog+","+currLongDialog;
        Uri gmmIntentUri = Uri.parse(longlatString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startActivity(mapIntent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_detail_menu, menu);

        /*String offset ="    ";
        for (int i=0;i<2;i++) {
            MenuItem item = menu.getItem(i);
            SpannableString s = new SpannableString(offset+item.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
                    0,s.length(), 0);
            item.setTitle(s);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       /* if (id == R.id.view_group_members_menu) {
            //TODO:
            showToast(currentActivity, "TODO");
            return true;
        }else if (id == R.id.group_notification_pefs_menu){
            //TODO:
            showToast(currentActivity, "TODO");
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void goToChat(View view){
        Intent intent=new Intent(GroupDetailActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    public void goToNotify(View view){
        Intent intent=new Intent(GroupDetailActivity.this, NotifyFreeParkActivty.class);
        startActivity(intent);
    }

    public void goToAsk(View view){
    }

}
