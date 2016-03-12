package com.luminous.dsys.youthconnect.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.luminous.dsys.youthconnect.activity.DocListActivity;
import com.luminous.dsys.youthconnect.activity.MainActivity;
import com.luminous.dsys.youthconnect.activity.QAAnsweredActivity;
import com.luminous.dsys.youthconnect.activity.QAPendingActivity;
import com.luminous.dsys.youthconnect.activity.QAPublishedActivity;
import com.luminous.dsys.youthconnect.activity.SettingsActivity;
import com.pushbots.push.PBNotificationIntent;
import com.pushbots.push.Pushbots;
import com.pushbots.push.utils.PBConstants;

import java.util.HashMap;

/**
 * Created by Android Luminous on 3/4/2016.
 */
public class customHandler extends BroadcastReceiver
{
    private static final String TAG = "customHandler";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);
        // Handle Push Message when opened
        if (action.equals(PBConstants.EVENT_MSG_OPEN)) {
            //Check for Pushbots Instance
            Pushbots pushInstance = Pushbots.sharedInstance();
            if (!pushInstance.isInitialized()) {
                Log.d(TAG, "Initializing Pushbots.");
                Pushbots.sharedInstance().init(context.getApplicationContext());
            }

            //Clear Notification array
            if (PBNotificationIntent.notificationsArray != null) {
                PBNotificationIntent.notificationsArray = null;
            }

            HashMap<?, ?> PushdataOpen = (HashMap<?, ?>) intent.getExtras().get(PBConstants.EVENT_MSG_OPEN);
            Log.w(TAG, "User clicked notification with Message: " + PushdataOpen.get("message"));

//            Report Opened Push Notification to Pushbots
            if (Pushbots.sharedInstance().isAnalyticsEnabled()) {
                Pushbots.sharedInstance().reportPushOpened((String) PushdataOpen.get("PUSHANALYTICS"));
            }

            Bundle bundle = intent.getBundleExtra("pushData");
            String activity = bundle.getString("activity", "");
            String fragment = bundle.getString("fragment", "");

            if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.MainActivity")) {
                // MainActivity Dashboard
                if(!MainActivity.isActive){
                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    resultIntent.putExtra("fragment", fragment);
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            } else if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.DocListActivity")) {
                // DocList
                if(!DocListActivity.isActive){
                    Intent resultIntent = new Intent(context, DocListActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    resultIntent.putExtras(intent.getBundleExtra("pushData"));
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            } else if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.QAPublishedActivity")) {
                // QAPublished
                if(!QAPublishedActivity.isActive){
                    Intent resultIntent = new Intent(context, QAPublishedActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    resultIntent.putExtras(intent.getBundleExtra("pushData"));
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            } else if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.QAAnsweredActivity")) {
                // QAAnswered
                if(!QAAnsweredActivity.isActive){
                    Intent resultIntent = new Intent(context, QAAnsweredActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    resultIntent.putExtras(intent.getBundleExtra("pushData"));
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            } else if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.QAPendingActivity")) {
                // QAUnAnswered
                if(!QAPendingActivity.isActive){
                    Intent resultIntent = new Intent(context, QAPendingActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    resultIntent.putExtras(intent.getBundleExtra("pushData"));
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            } else if (activity.equalsIgnoreCase("com.luminous.dsys.youthconnect.activity.SettingsActivity")) {
                // Change Password
                if(!SettingsActivity.isActive){
                    Intent resultIntent = new Intent(context, SettingsActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    resultIntent.putExtras(intent.getBundleExtra("pushData"));
                    Pushbots.sharedInstance().startActivity(resultIntent);
                }
            }

            //Start lanuch Activity
            /*String packageName = context.getPackageName();
            Intent resultIntent = new Intent(context.getPackageManager().getLaunchIntentForPackage(packageName));
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

            resultIntent.putExtras(intent.getBundleExtra("pushData"));
            Pushbots.sharedInstance().startActivity(resultIntent);*/

            // Handle Push Message when received
        }else if(action.equals(PBConstants.EVENT_MSG_RECEIVE)){
            HashMap<?, ?> PushdataOpen = (HashMap<?, ?>) intent.getExtras().get(PBConstants.EVENT_MSG_RECEIVE);
            Log.w(TAG, "User Received notification with Message: " + PushdataOpen.get("message"));
        }
    }
}
