package com.theironfoundry8890.stjohns;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class resultsActivity extends AppCompatActivity {

    private String expTitle;
    private String expDesc;
    private String expPublishDate;
    private String expEventDate;
    private String expFullName;
    private String expRealPublishDate;
    private String expFileAttachment;

    private String generatedSrc;

    private int a = 4;
    private float x1, x2;
    static final int MIN_DISTANCE = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.t_activity_results);
        EditText rollNoLastThreeDigitsEditText = (EditText) findViewById(R.id.rollNoLastThreeDigits);
        rollNoLastThreeDigitsEditText.requestFocus();
        colorCheck();


    }

    public void onClickGetResults(View v) {

        //Loading Data via ShredPreferences
        EditText rollNoEditText = (EditText) findViewById(R.id.rollNo);
        String derivedRollNo = String.valueOf(rollNoEditText.getText());
        EditText rollNoLastThreeDigitsEditText = (EditText) findViewById(R.id.rollNoLastThreeDigits);
        rollNoLastThreeDigitsEditText.setSelection(rollNoLastThreeDigitsEditText.length());
        String derivedRollNoLastThreeDigits = String.valueOf(rollNoLastThreeDigitsEditText.getText());
        if(derivedRollNoLastThreeDigits.length()>0) {


            generatedSrc = "https://dbrauaaems.in/ResultDispBBAI_SEM_1718.aspx?rl=" + derivedRollNo + derivedRollNoLastThreeDigits;
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.setVisibility(View.VISIBLE);
            myWebView.getSettings().setBuiltInZoomControls(true);
            myWebView.getSettings().setDisplayZoomControls(false);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            myWebView.loadUrl(generatedSrc);
        }else
        {
            Toast.makeText(this, "Please Enter the remaining digits" , Toast.LENGTH_SHORT).show();
        }

    }


    public void onClickFullScreen(View v) {

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

    public void IntializeData() {
        //Calling ids from layout
        TextView titleTextView = (TextView) findViewById(R.id.detailedlaytitle);
        TextView descTextView = (TextView) findViewById(R.id.detailedlaydesc);
        TextView fullNameTextView = (TextView) findViewById(R.id.publisher);
        TextView dateTextView = (TextView) findViewById(R.id.date);

        //Setting values to layouts
        titleTextView.setText(expTitle);
        descTextView.setText(expDesc);
        fullNameTextView.setText(expFullName);
        dateTextView.setText(expRealPublishDate);


    }

    public void onClickAttendance(View v) {
        Intent selectIntent = new Intent(resultsActivity.this, feedbackWriter.class);
        startActivity(selectIntent);


    }

    public void onClickAnnouncement(View v) {
        Intent selectIntent = new Intent(resultsActivity.this, t_Announcement_Viewer.class);
        startActivity(selectIntent);


    }

    public void onClickNotes(View v) {
        Intent selectIntent = new Intent(resultsActivity.this, t_notes_Viewer.class);
        startActivity(selectIntent);


    }

    public void onClickEvents(View v) {
        Intent selectIntent = new Intent(resultsActivity.this, EventViewer.class);
        startActivity(selectIntent);


    }

    public void onClickProfile(View v) {
        Intent selectIntent = new Intent(resultsActivity.this, Newsfeed.class);
        startActivity(selectIntent);


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
