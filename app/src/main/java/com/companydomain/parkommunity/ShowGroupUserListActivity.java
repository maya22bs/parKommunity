package com.companydomain.parkommunity;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.companydomain.parkommunity.Database.didIfinished;
import static com.companydomain.parkommunity.Database.usersList;

public class ShowGroupUserListActivity extends AppCompatActivity {


    public static final String TAG = "TAG_"+ShowGroupUserListActivity.class.getName();

    Group currentGroup;
    List<User> userList;

    ListView listView;

    private Handler mHandler;
    private Runnable mHandlerTask;

    public class UserViewUdapter extends BaseAdapter implements View.OnClickListener {

        ArrayList<User> data;
        LayoutInflater inflater;
        Context context;
        MyViewHolder mViewHolder;


        public UserViewUdapter(Context context, ArrayList<User> data) {
            this.data = data;
            this.context = context;
            inflater = LayoutInflater.from(this.context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.user_view_node, parent, false);
                mViewHolder = new MyViewHolder(convertView);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            User currentUserData = (User)getItem(position);
            mViewHolder.name.setText(currentUserData.getName());
//            mViewHolder.ivIcon.setImageResource(R.drawable.com_facebook_button_icon);//TODO need to fix image
            Picasso.with(ShowGroupUserListActivity.this).load(currentUserData.getPhotoUrl()).error(R.drawable.ic_menu_camera).into(mViewHolder.ivIcon);

            return convertView;
        }

        @Override
        public void onClick(View v) {

        }

        private class MyViewHolder {
            TextView name;
            ImageView ivIcon;

            public MyViewHolder(View item) {
                name = (TextView) item.findViewById(R.id.textView);
                ivIcon = (ImageView) item.findViewById(R.id.imageView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group_user_list);

        mHandler = new Handler();
        currentGroup = GroupDetailActivity.currentGroup;
        setTitle(currentGroup.getName() +" - members");

        userList = new ArrayList<>();

        Database.didIfinished = false;
        Database.getUsersListFromGroup(currentGroup);
        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                if ( !didIfinished) {
                    Log.d("TAG_", "waiting--> waiting");
                    mHandler.postDelayed(mHandlerTask, 200);  // 0.2 second delay
                }else {
                    Log.d("TAG_", "waitng--> success");
                    setAdapterUserList(usersList);
                    mHandler.removeCallbacks(mHandlerTask);
                }
            }
        };

        mHandlerTask.run();


    }

    private void setAdapterUserList(ArrayList<User> userList){
        listView = (ListView)findViewById(R.id.lvUserList);
        listView.setAdapter(new UserViewUdapter(getApplicationContext(), (ArrayList<User>) userList));

    }
}
