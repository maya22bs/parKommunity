package com.companydomain.parkommunity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static android.view.View.GONE;
import static com.companydomain.parkommunity.Database.addUserToGroup;
import static com.companydomain.parkommunity.Database.static_myGroups;
import static com.companydomain.parkommunity.Database.static_otherGroups;
import static com.companydomain.parkommunity.GroupDetailActivity.currentGroup;
import static com.companydomain.parkommunity.GroupDetailActivity.userEnrolled;

public class GroupsListActivity extends AppCompatActivity {

    private static final String TAG = "TAG_"+ GroupsListActivity.class.getSimpleName();

    private Activity currentActivity = this;
    public static final int REQUEST_CODE_ENROLLED =123;
    private final int MAX_AUTO_LOAD =4;
    private float offset;
    private Interpolator interpolator;
    private int lastAnimatedPosition = 0;

    private static MessageHolder currentHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);

        offset = getResources().getDimensionPixelSize(R.dimen.offset_y);
        interpolator = AnimationUtils.loadInterpolator(currentActivity, android.R.interpolator.linear_out_slow_in);


        populateView();

    }


    private void populateView(){
        RecyclerView list = (RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new MessageAdapter(this));
        list.setHasFixedSize(true);


    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

        private final int[] COLORS = new int[] { getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.Buttons),
                                                  };
        private Activity host;
        private final LayoutInflater inflater;

        public MessageAdapter(Activity activity) {
            host = activity;
            inflater = LayoutInflater.from(host);
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessageHolder(inflater.inflate(R.layout.message, parent, false));
        }

        @Override
        public void onBindViewHolder(final MessageHolder holder, final int position) {
            final int color = COLORS[position % COLORS.length];

            holder.avatar.setBackgroundTintList(ColorStateList.valueOf(color));

            Log.d(TAG, "position: "+position);

            if(position < lastAnimatedPosition || position > MAX_AUTO_LOAD){
                showViewWithoutAnimation(holder.avatar);
                showViewWithoutAnimation(holder.title);
                if(position < static_myGroups.size()) {
                    showViewWithoutAnimation(holder.subtitle);
                    showViewWithoutAnimation(holder.subtitle2);
                    holder.enrollButton.setVisibility(View.GONE);
                }else{
                    holder.subtitle.setVisibility(GONE);
                    holder.subtitle2.setVisibility(GONE);
                    showViewWithoutAnimation(holder.enrollButton);
                }
            }else {
                lastAnimatedPosition++;
                animateViewIn(holder.avatar);
                animateViewIn(holder.title);

                if(position < static_myGroups.size()) {
                    animateViewIn(holder.subtitle);
                    animateViewIn(holder.subtitle2);
                    holder.enrollButton.setVisibility(View.GONE);
                }else{
                    holder.subtitle.setVisibility(GONE);
                    holder.subtitle2.setVisibility(GONE);
                    animateViewIn(holder.enrollButton);
                }
            }
            // increase the offset distance for the next view
            offset *= 1.5f;

            final Group group;
            if(position < static_myGroups.size()) {
                group = static_myGroups.get(position);
                holder.title.setText(group.getName());
                holder.subtitle.setText("number of users: " + static_myGroups.get(position).getUsers().size());
                holder.subtitle2.setText("enrolled to group");
                holder.subtitle2.setBackgroundColor(Color.parseColor("#4CAF50"));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(host, GroupDetailActivity.class);
                        boolean curve = true;//(position % 2 == 0);
                        intent.putExtra(GroupDetailActivity.EXTRA_COLOR, color);
                        intent.putExtra(GroupDetailActivity.EXTRA_CURVE, curve);
                        intent.putExtra(GroupDetailActivity.EXTRA_POSITION, position);
                        currentGroup = group;
                        currentHolder = holder;
                        host.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                                host, holder.avatar, holder.avatar.getTransitionName()).toBundle());

                    }
                });

            }else{
                group = static_otherGroups.get(position - static_myGroups.size());
                holder.title.setText(group.getName());
                //holder.subtitle.setText("number of users: " + static_otherGroups.get(position - static_myGroups.size()).getUsers().size());
                //holder.subtitle2.setText("not enrolled to group");
                //holder.subtitle2.setBackgroundColor(Color.parseColor("#c62828"));
                holder.subtitle.setVisibility(GONE);
                holder.subtitle2.setVisibility(GONE);
                holder.enrollButton.setVisibility(View.VISIBLE);
                holder.enrollButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        addUserToGroup(currentActivity, group);

                        Intent intent = new Intent(host, GroupDetailActivity.class);
                        boolean curve = true;//(position % 2 == 0);
                        intent.putExtra(GroupDetailActivity.EXTRA_COLOR, color);
                        intent.putExtra(GroupDetailActivity.EXTRA_CURVE, curve);
                        intent.putExtra(GroupDetailActivity.EXTRA_POSITION, position);
                        currentGroup = group;
                        currentHolder = holder;
                        host.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                                host, holder.avatar, holder.avatar.getTransitionName()).toBundle());

                        currentHolder.subtitle.setText("number of users: " + currentGroup.getUsers().size());
                        currentHolder.subtitle2.setText("enrolled to group");
                        currentHolder.subtitle2.setBackgroundColor(Color.parseColor("#4CAF50"));
                        currentHolder.subtitle.setVisibility(View.VISIBLE);
                        currentHolder.subtitle2.setVisibility(View.VISIBLE);
                        currentHolder.enrollButton.setVisibility(View.GONE);

                    }
                });
            }


        }

        @Override
        public int getItemCount() {
            return static_myGroups.size() + static_otherGroups.size();
        }
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView title;
        TextView subtitle;
        TextView subtitle2;
        Button enrollButton;

        public MessageHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            subtitle2 = (TextView) itemView.findViewById(R.id.subtitle2);
            enrollButton = (Button) itemView.findViewById(R.id.buttonEnroll);
        }
    }

    private void showViewWithoutAnimation(View view){
       // Log.d("tag3", "here");
        view.setVisibility(View.VISIBLE);
        //view.setTranslationY(offset);
        //view.setAlpha(0.85f);
    }

    private void animateViewIn(View view){
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(offset);
        view.setAlpha(0.85f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(interpolator)
                .setDuration(1000L)
                .start();
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d("tag5","here2");
        if(currentHolder != null && userEnrolled){
            Log.d("tag5", "here3");
            userEnrolled = false;
            currentHolder.subtitle.setText("number of users: " + currentGroup.getUsers().size());
            currentHolder.subtitle2.setText("enrolled to group");
            currentHolder.subtitle2.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
    }


}
