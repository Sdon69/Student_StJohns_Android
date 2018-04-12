package com.theironfoundry8890.stjohns;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class send_firebase_notification  {



    public static void sendGcmMessage(String messageTitle, String messageBody)
    {
        if(messageTitle!=null || messageBody!=null) {
            messageTitle = messageTitle.replace("<comma3384>", ".");
            messageBody = messageBody.replace("<comma3384>", ".");
            String envelope = messageTitle + "<comma3384>" + messageBody;

            new RetrieveFeedTask().execute(envelope);


        }
    }

    static class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;


        protected String doInBackground(String[] args) {

            if (args.length < 1 || args.length > 2 || args[0] == null) {
                System.err.println("usage: ./gradlew run -Pmsg=\"MESSAGE\" [-Pto=\"DEVICE_TOKEN\"]");
                System.err.println("");
                System.err.println("Specify a test message to broadcast via GCM. If a device's GCM registration token is\n" +
                        "specified, the message will only be sent to that device. Otherwise, the message \n" +
                        "will be sent to all devices subscribed to the \"global\" topic.");
                System.err.println("");
                System.err.println("Example (Broadcast):\n" +
                        "On Windows:   .\\gradlew.bat run -Pmsg=\"<Your_Message>\"\n" +
                        "On Linux/Mac: ./gradlew run -Pmsg=\"<Your_Message>\"");
                System.err.println("");
                System.err.println("Example (Unicast):\n" +
                        "On Windows:   .\\gradlew.bat run -Pmsg=\"<Your_Message>\" -Pto=\"<Your_Token>\"\n" +
                        "On Linux/Mac: ./gradlew run -Pmsg=\"<Your_Message>\" -Pto=\"<Your_Token>\"");
                System.exit(1);
            }
            try {
                // Prepare JSON containing the GCM message content. What to send and where to send.
                JSONObject jGcmData = new JSONObject();
                JSONObject jData = new JSONObject();
                try {
                    jData.put("message", args[0].trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Where to send GCM message.
                if (args.length > 1 && args[1] != null) {
                    try {
                        jGcmData.put("to", args[1].trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        jGcmData.put("to", "/topics/Management1");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // What to send in GCM message.
                try {
                    jGcmData.put("data", jData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Create connection to send GCM Message request.
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + "AIzaSyDhyP7p8FDixgOyGy0KdbHMXRRFCvaXpWc");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Send GCM message content.
                OutputStream outputStream = conn.getOutputStream();
                Log.v("OutputStream", String.valueOf(conn));
                String jgcm = jGcmData.toString();
                jgcm = jGcmData.toString().replace("\\","");
                Log.v("jGcmData",jgcm);
                outputStream.write(jgcm.getBytes());

                // Read GCM response.
                InputStream inputStream = conn.getInputStream();
                String resp = IOUtils.toString(inputStream);
                System.out.println(resp);
                System.out.println("Check your device/emulator for notification or logcat for " +
                        "confirmation of the receipt of the GCM message.");
            } catch (IOException e) {
                System.out.println("Unable to send GCM message.");
                System.out.println("Please ensure that API_KEY has been replaced by the server " +
                        "API key, and that the device's registration token is correct (if specified).");
                e.printStackTrace();
            }
            return args[0];
        }
        protected void onPostExecute(String feed) {

            Log.v("postfeed",feed);

        }
    }

}
