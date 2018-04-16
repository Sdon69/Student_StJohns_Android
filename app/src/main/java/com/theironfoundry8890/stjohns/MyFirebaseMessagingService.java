package com.theironfoundry8890.stjohns;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public MyFirebaseMessagingService() {
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String TAG = "Message Recieved";
        // ...
        Log.v("message Recieved", String.valueOf(remoteMessage));
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            generateNotification(String.valueOf(remoteMessage.getData()));

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void generateNotification(String body) {






        if (body != null) {
            body = body.substring(body.indexOf("{message=") + 9, body.lastIndexOf("}"));
            Log.v("notificationText", body);
            String bodyArray[] = body.split("<comma3384>");
            String notificationTitle = "Title";
            String notificationDescription = "Body";
            if(bodyArray[0] != null)
            {
                 notificationTitle = bodyArray[0];
            }


            if(bodyArray[1] != null)
            {
                 notificationDescription = bodyArray[1];
            }




            Intent notifyIntent = new Intent(this, Newsfeed.class);


// Set the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // Create the PendingIntent
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );


            SharedPreferences mPrefs = getSharedPreferences("label", 0);
            int notificationId = mPrefs.getInt("notificationId", 0);
            int SUMMARY_ID = 0;
            String GROUP_KEY_WORK_EMAIL = "com.theironfoundry8890.stjohns";

            Notification newMessageNotification1;
             newMessageNotification1 =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationDescription)
                            .setGroup(GROUP_KEY_WORK_EMAIL)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentIntent(notifyPendingIntent)
                            .build();



            Notification summaryNotification =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setContentTitle("True Summary")
                            //set content text to support devices running API level < 24
                            .setContentText("Two new messages")
                            .setSmallIcon(R.drawable.notification_icon)
                            //build summary info into InboxStyle template
                            .setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(" ")
                                    .addLine(" ")
                                    .setBigContentTitle(" ")
                                    .setSummaryText("Pending Notifications"))
                            //specify which group this notification belongs to
                            .setGroup(GROUP_KEY_WORK_EMAIL)
                            //set this notification as the summary for the group
                            .setGroupSummary(true)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSound(uri)
                            .setVibrate(new long[] {1, 1, 1})
                            .setContentIntent(notifyPendingIntent)
                            .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(++notificationId, newMessageNotification1);
            notificationManager.notify(SUMMARY_ID, summaryNotification);


            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putInt("notificationId", notificationId).apply();

        }
    }


}