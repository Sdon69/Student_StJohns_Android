package com.theironfoundry8890.stjohns;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class t_Detailed_Notes extends AppCompatActivity {

    private String expTitle;
    private String expDesc;
    private String expPublishDate;
    private String expEventDate;
    private String expFullName;
    private String expFileAttachment;

    private int a = 2;
    private float x1,x2;
    static final int MIN_DISTANCE = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.t_activity_detailed_notes);
        loadData();
        colorCheck();


    }

    public void loadData(){

        //Loading Data via ShredPreferences
        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        String TitleString = mPrefs.getString("title", "default_value_if_variable_not_found");
        String DescriptionString = mPrefs.getString("desc", "default_value_if_variable_not_found");
        String fullName = mPrefs.getString("fullName", "default_value_if_variable_not_found");
        String fileAttachment = mPrefs.getString("fileAttachment", "default_value_if_variable_not_found");





        //Initializing using above variables
        expTitle = TitleString;
        expDesc = DescriptionString;
        expFullName = fullName;
        expFileAttachment = fileAttachment;

        if(fileAttachment.trim().equals("None")) {

            RelativeLayout webViewLayout = (RelativeLayout) findViewById(R.id.webViewLayout);

            webViewLayout.setVisibility(View.INVISIBLE);

        }else
        {

            Log.v("None",fileAttachment);
            String trimmedFileId = fileAttachment.substring(fileAttachment.lastIndexOf("/d/") + 3, fileAttachment.lastIndexOf("vi") - 1);
            String sourceString = "https://docs.google.com/viewer?srcid=fileid&pid=explorer&efh=false&a=v&chrome=false&embedded=true";
            String generatedSrc = sourceString.replace("fileid", trimmedFileId);
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.setWebViewClient(new WebViewClient());
            myWebView.loadUrl(generatedSrc);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
        }




        IntializeData();






    }
    public void onClickFullScreen(View v) {

        String trimmedFileId = expFileAttachment.substring(expFileAttachment.lastIndexOf("/d/") + 3, expFileAttachment.lastIndexOf("vi") - 1);
        String sourceString = "https://docs.google.com/viewer?srcid=fileid&pid=explorer&efh=false&a=v&chrome=false&embedded=true";
        String generatedSrc = sourceString.replace("fileid", trimmedFileId);
        Uri webpage = Uri.parse(generatedSrc);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(webIntent);
    }

    public void onClickDownload(View v) {

        String trimmedFileId = expFileAttachment.substring(expFileAttachment.lastIndexOf("/d/") + 3, expFileAttachment.lastIndexOf("vi") - 1);
        String sourceString = "https://drive.google.com/uc?export=download&id=fileid";
        String generatedSrc = sourceString.replace("fileid", trimmedFileId);
        Uri webpage = Uri.parse(generatedSrc);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(webIntent);
    }


    public void IntializeData()
    {
        //Calling ids from layout

        TextView titleTextView = (TextView) findViewById(R.id.detailedlaytitle);
        TextView descTextView = (TextView) findViewById(R.id.detailedlaydesc);
        TextView fullNameTextView = (TextView) findViewById(R.id.publisher);

        //Setting values to layouts
        titleTextView.setText(expTitle);
        descTextView.setText(expDesc);
        fullNameTextView.setText(expFullName);




    }

    public void onClickAttendance(View v)
    {
        Intent selectIntent = new Intent(t_Detailed_Notes.this,feedbackWriter.class);
        startActivity(selectIntent);


    }

    public void onClickAnnouncement(View v)
    {
        Intent selectIntent = new Intent(t_Detailed_Notes.this,t_Announcement_Viewer.class);
        startActivity(selectIntent);


    }

    public void onClickNotes(View v)
    {
        Intent selectIntent = new Intent(t_Detailed_Notes.this,t_notes_Viewer.class);
        startActivity(selectIntent);


    }

    public void onClickEvents(View v)
    {
        Intent selectIntent = new Intent(t_Detailed_Notes.this,EventViewer.class);
        startActivity(selectIntent);


    }

    public void onClickProfile(View v)
    {
        Intent selectIntent = new Intent(t_Detailed_Notes.this,Newsfeed.class);
        startActivity(selectIntent);


    }


    private void swipe() {

        TextView head2 = (TextView) findViewById(R.id.head2);
        TextView head1 = (TextView) findViewById(R.id.head);

        Button button1 = (Button) findViewById(R.id.Button1);
        Button button2 = (Button) findViewById(R.id.Button2);
        Button button3 = (Button) findViewById(R.id.Button3);



        if(a==0){

            Intent selectIntent = new Intent(t_Detailed_Notes.this,feedbackWriter.class);
            startActivity(selectIntent);

        }


        if(a==1) {

            Intent selectIntent = new Intent(t_Detailed_Notes.this,t_Announcement_Viewer.class);
            startActivity(selectIntent);


        }

        if(a==2) {
            Intent selectIntent = new Intent(t_Detailed_Notes.this,t_notes_Viewer.class);
            startActivity(selectIntent);
        }

        if(a==3) {
            Intent selectIntent = new Intent(t_Detailed_Notes.this,EventViewer.class);
            startActivity(selectIntent);

        }

        if(a==4){
            Intent selectIntent = new Intent(t_Detailed_Notes.this,Newsfeed.class);
            startActivity(selectIntent);

        }









    }



    private void colorCheck() {

        ImageView attendanceImageView = (ImageView) findViewById(R.id.attendance);
        ImageView announcementImageView = (ImageView) findViewById(R.id.announcement);
        ImageView notesImageView = (ImageView) findViewById(R.id.notes);
        ImageView eventsImageView = (ImageView) findViewById(R.id.events);
        ImageView profileImageView = (ImageView) findViewById(R.id.profile);


        if (a == 0) {

            attendanceImageView.setImageResource(R.drawable.attendance_grey);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.newsfeed);

        }
        if (a == 1) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements_grey);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.newsfeed);
        }

        if (a == 2) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes_grey);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.newsfeed);
        }

        if (a == 3) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events_grey);
            profileImageView.setImageResource(R.drawable.newsfeed);
        }

        if (a == 4) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.newsfeed_grey);
        }


    }
}
