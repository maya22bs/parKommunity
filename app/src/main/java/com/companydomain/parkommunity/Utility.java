package com.companydomain.parkommunity;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Toast;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by ori on 26/08/16.
 */

public class Utility {

    private static final String TAG = "TAG_"+ Utility.class.getSimpleName();


    public static void savePrefs(Context context, String key, Set<String> value) {
        // TODO Auto-generated method stub
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public static void savePrefs(Context context, String key, boolean value) {
        // TODO Auto-generated method stub
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean isAppInForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equalsIgnoreCase(context.getString(R.string.app_package))) {
            return true;
        } else {
            return false;
        }
    }

    public static String getUniqueGroupId() {
        return UUID.randomUUID().toString();
    }


    private static Intent getStartNavigateIntent(String latitude, String longtitude) {
        try {
            String url = "waze://?ll=" + latitude+","+longtitude+"&navigate=yes";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            return intent;
        } catch (ActivityNotFoundException ex) {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            return intent;
        }
    }

    public static void showNotifyNotificationTest(Context context, ParkingSpot spot) {

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setLargeIcon(bm)
                .setContentTitle("Free park!") // title for notification
                .setContentText(spot.getWhoGeneratedMe()+" notified free park at: ") // message for notification
                .setSubText(spot.getAddress())
                .setAutoCancel(true); // clear notification after click
        Intent navigationIntent = getStartNavigateIntent(spot.getLatitude(),spot.getLongitude());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"));

        String longlatString="geo: "+ spot.getLatitude()+","+spot.getLongitude();
        String myLabel ="label";
        String myName = "name";
        String params = "?q="+spot.getLatitude()+","+spot.getLongitude()+"("+myLabel+"+"+myName+")";
        Uri gmmIntentUri = Uri.parse(longlatString+params);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        PendingIntent navigationPendingIntent = PendingIntent.getActivity(context, 0, navigationIntent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent mapPendingIntent = PendingIntent.getActivity(context, 0, mapIntent, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentIntent(navigationPendingIntent);
        mBuilder.setContentIntent(mapPendingIntent);

        mBuilder.addAction(R.mipmap.ic_navigation_white_24dp, "Navigate", navigationPendingIntent);
        mBuilder.addAction(R.mipmap.ic_place_white_24dp, "Show On Map", mapPendingIntent);


        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification=mBuilder.build();
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainIntentPending = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_ONE_SHOT);
        notification.contentIntent = mainIntentPending;
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(0,notification);

    }
    public static void showNotificationAskForHelp(Context context, Group group) {

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("HELP!") // title for notification
                .setContentText(group.getAskForHelpGroupName()+": "+group.getWhoGeneratedAskForHelpUserName()+" is asking for help!") // message for notification
                .setAutoCancel(true); // clear notification after click

        Intent notifyIntent = new Intent(context, NotifyFreeParkActivty.class);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.addAction(R.mipmap.ic_sms_failed_white_24dp, "Notify Free Park", notifyPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification=mBuilder.build();



        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainIntentPending = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_ONE_SHOT);
        notification.contentIntent = mainIntentPending;

        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0, notification);



    }

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static int dpToPixel(Context context, int dp) {
        // dp to pixel
        Resources r = context.getResources();

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public static double stringToDouble(String str) {
        return Double.parseDouble(str);
    }

    public static String doubleToString(Double dub) {
        return String.valueOf(dub);
    }

    public static String parsedAddress(String str){
        int position=str.indexOf("\n");
        if(str.length()==0){
            return "";
        }
        Log.d("position is",position+"");
        if(position==-1){
            return str;
        }
        String resultString=str.substring(0,position)+" "+str.substring(position+1);
        return resultString;
    }

}
