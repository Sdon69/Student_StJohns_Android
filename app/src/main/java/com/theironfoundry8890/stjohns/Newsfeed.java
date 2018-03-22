package com.theironfoundry8890.stjohns;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Newsfeed extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private Button mCallApiButton;
    ProgressDialog mProgress;


    private String sSemester;
    private String sDepartment;
    private String globalTransferArray[];



    private TextView mOutputText;
    private String sPassword;private String userId;
    private boolean idAvailcheck = true;
    private String mode = "announcementViewer" ;

    private int a = 1;
    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    private String fullName;


    private String title;
    private String description;
    private String publishDate;
    private String eventDate;
    private String publishId;
    private String departments;

    private String lastDateofRegistration;
    private String fees;
    final ArrayList<newsfeedPublic> words = new ArrayList<newsfeedPublic>();
    final ArrayList<newsfeedContainer> containers = new ArrayList<newsfeedContainer>();

    private String dept_filter = "All Departments";
    private String semester_filter = "All Semesters";

    private String dFName;
    private String dLName;
    private String dClass;
    private String dEmail;
    private String dSection;
    private String dId;
    private String dPassword;
    private String dPhone;
    private List dRow;

    private int dayIndex;

    private boolean end = true;

    private int tableNo;
    private String gSavedAnnSheetId;
    private String savedPass;
    private String savedId;




    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Sheets API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.newsfeed);




        LinearLayout activityLayout = (LinearLayout) findViewById(R.id.mLayout);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);


        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                " ");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading ...");
        mProgress.setCanceledOnTouchOutside(false);

        setContentView(activityLayout);

        colorCheck();
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        loadData();

        dept_filter = sDepartment;

        semester_filter = sSemester;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);


         dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        timetableDataConversion(dayIndex);

        getResultsFromApi();

    }



    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {

            Toast.makeText(getApplicationContext(), "No Network Connection",
                    Toast.LENGTH_SHORT).show();
            mOutputText.setText("No network connection available.");
        } else {


            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            SharedPreferences mPrefs = getSharedPreferences("label", 0);userId = mPrefs.getString("UserId", "default_value_if_variable_not_found");String accountName = userId;
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        Log.v("t_Announcement_Viewer" , "Success");
        return connectionStatusCode == ConnectionResult.SUCCESS;

    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                Newsfeed.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = "116OBhXliG69OB5bKRAEwpmlOz21LCCWStniSuIR6wPI";
            int a = 2;
            idAvailcheck = true;
            String range = "Stj Teacher Notes!".concat("A"+ a++ + ":I");
            end = false;



            ValueRange oRange = new ValueRange();



            List<ValueRange> oList = new ArrayList<>();
            oList.add(oRange);


            BatchUpdateValuesRequest oRequest = new BatchUpdateValuesRequest();
            oRequest.setValueInputOption("RAW");
            oRequest.setData(oList);



            if(mode.equals("notesViewer")) {
                spreadsheetId = "1UDDtel5vAFBqVnaPZIZl20SwZEz_7fxGXYQOuKLvSmQ";
            }else if(mode.equals("eventViewer"))
            {
                spreadsheetId = "1SC0UPYthsoS5NKDuC5oJt-y29__f0gm0wkIkJoDduWw";
                range = "Events!".concat("A"+ 2 + ":S");
            }
//            else if(mode.equals("timeTable"))
//            {
//
//
//            }



            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {


                results.add("");


                for (List row : values) {


                    if(mode.equals("announcementViewer")) {

                        dId = String.valueOf(row.get(0));

                        dRow = row;

                        if (dId.contains("BonBlank88")) {

                            end = true;

                            continue;
                        }

                        String cataegories = String.valueOf(row.get(2));

                        if (cataegories.contains(dept_filter)) {
                            if (cataegories.contains(semester_filter)) {

                                title = String.valueOf(row.get(0));
                                description = String.valueOf(row.get(1));
                                String publisherId = String.valueOf(row.get(3));//Departments
                                fullName = String.valueOf(row.get(4));
                                String uniqueId = String.valueOf(row.get(5));
                                String datePublished = String.valueOf(row.get(6));
                                String fileAttachment = String.valueOf(row.get(7));

                                words.add(new newsfeedPublic(description, title, cataegories, publisherId, fullName, uniqueId, datePublished
                                        , fileAttachment,"ANNOUNCEMENTS"));
                            }
                        }


                    } //End of announcementViewer Mode


                    if(mode.equals("notesViewer")) {

                        dId = String.valueOf(row.get(0));

                        if (dId.contains("BonBlank88"))
                        {

                            end = true;

                            continue;
                        }

                        String cataegories = String.valueOf(row.get(2));

                        if(cataegories.contains(dept_filter)) {
                            if(cataegories.contains(semester_filter)) {
                                title = String.valueOf(row.get(0));
                                description = String.valueOf(row.get(1));
                                String publisherId = String.valueOf(row.get(3));//Departments
                                fullName = String.valueOf(row.get(4));
                                String uniqueId = String.valueOf(row.get(5));
                                String datePublished = String.valueOf(row.get(6));
                                String fileAttachment = String.valueOf(row.get(7));

                                words.add(new newsfeedPublic(description, title, cataegories, publisherId, fullName, uniqueId,datePublished
                                        ,fileAttachment, "NOTES"));
                            }
                        }

                    }  //End of notesViewer mode

                    else if(mode.equals("eventViewer"))
                    {


                        String str1 = String.valueOf(row.get(0));

                        if (str1.contains("BonBlank88"))
                        {

                            end = true;

                            continue;
                        }
          String status = String.valueOf(row.get(9));
    if(status.equals("A")) {




                                title = String.valueOf(row.get(0));
                                description = String.valueOf(row.get(1));

                                String fullName = String.valueOf(row.get(8));
                                publishDate = String.valueOf(row.get(2));
                                eventDate = String.valueOf(row.get(3));
                                lastDateofRegistration = String.valueOf(row.get(4));
                                fees = String.valueOf(row.get(5));
                                fees = " ₹".concat(fees);



                                words.add(new newsfeedPublic(description, title, publishDate, eventDate,
                                        lastDateofRegistration, fees, fullName,"EVENTS"));

                        }
                    } //End of eventViewer Mode
                }
            }

            return results;

        }




        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();



        }



        @Override
        protected void onPostExecute(List<String> output) {

            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");

            } else {
                output.add(0, " ");
                mOutputText.setText(TextUtils.join("\n", output));

                end = true;

                if(mode.equals("announcementViewer")) {
                    mode = "notesViewer";

                    getResultsFromApi();
                }
                else if(mode.equals("notesViewer")) {
                    mode = "eventViewer";

                    getResultsFromApi();
                }  else if(mode.equals("eventViewer"))
                {


                    String dateRetrieved = words.toString();
                    Log.v("trimmedString", dateRetrieved);
                    String trimmedString = dateRetrieved.replace("[","");
                    trimmedString = trimmedString.replace("]","");
                    Log.v("trimmedString", trimmedString);
                    trimmedString = trimmedString.replace(" NOTES","NOTES");
                    trimmedString = trimmedString.replace(" ANNOUNCEMENTS","ANNOUNCEMENTS");
                    trimmedString = trimmedString.replace(" EVENTS","EVENTS");
                    Log.v("trimmedString", trimmedString);


                    String dateArray[] = trimmedString.split(",");
                    String elementArray[];
                    String elementMode;



                    int i;

                    int latestIndex = 0;
                    String championDate = "date";
                    String descendingDates = "default";
                    String transferArray[] = {"hello","hello"};
                    String appendedChampionDate = ".";
                    Log.v("dateArray.length", String.valueOf(dateArray.length));
                    words.clear();
                    for(i=0;i<dateArray.length;i++) {

                        String latestDate;
//
                        elementArray = dateArray[i].split("%");
                        elementMode = elementArray[0];

                        Log.v("elementArray[0]", elementArray[0]);

                        if (elementMode.contains("ANNOUNCEMENTS") || elementMode.contains("NOTES")) {
                            latestDate = elementArray[7];

                        } else if (elementMode.contains("EVENTS")) {
                            latestDate = elementArray[3];
                        } else {
                            break;
                        }
//


                        int k = i;

                        for (k = k; k < dateArray.length; k++) {

                            String latestDataArray[] = latestDate.split("/");

                            int latestDateDay = Integer.parseInt(latestDataArray[0]);
                            int latestDateMonth = Integer.parseInt(latestDataArray[1]);
                            int latestDateYear = Integer.parseInt(latestDataArray[2]);


                            elementArray = dateArray[k].split("%");
                            elementMode = elementArray[0];
                            String challengeDate = "o";
                            if (elementMode.equals("ANNOUNCEMENTS") || elementMode.equals("NOTES")) {
                                challengeDate = elementArray[7];

                            } else if (elementMode.equals("EVENTS")) {
                                challengeDate = elementArray[3];
                            } else {
                                break;
                            }

                            String challengeDateArray[] = challengeDate.split("/");
                            Log.v("k", String.valueOf(k));
                            Log.v("latestDate", latestDate);
                            Log.v("challengeDate", challengeDate);
//                            Log.v("challengeDateArray", String.valueOf(challengeDate));
                            int challengeDateDay = Integer.parseInt(challengeDateArray[0]);
                            int challengeDateMonth = Integer.parseInt(challengeDateArray[1]);
                            int challengeDateYear = Integer.parseInt(challengeDateArray[2]);

                            boolean lastestDayIsPast = false;

                            if (latestDateYear < challengeDateYear) {
                                lastestDayIsPast = true;
                            } else if (latestDateMonth < challengeDateMonth) {
                                if (latestDateYear <= challengeDateYear) {
                                    lastestDayIsPast = true;
                                }
                            } else if (latestDateDay < challengeDateDay) {
                                if (latestDateMonth <= challengeDateMonth) {
                                    if (latestDateYear <= challengeDateYear) {
                                        lastestDayIsPast = true;

                                    }
                                }
                            }
//                            Log.v("lastestDayIsPast", String.valueOf(lastestDayIsPast));
                            if (lastestDayIsPast) {
                                championDate = challengeDate;
                                latestDate = challengeDate;
                                latestIndex = k;

                            }

                            lastestDayIsPast = false;


                        }
                        appendedChampionDate = appendedChampionDate + "  - \n " + latestDate;


                        transferArray[0] = dateArray[i];
                        dateArray[i] = dateArray[latestIndex];

                        dateArray[latestIndex] = transferArray[0];

                        Log.v("championDate", latestDate + latestIndex);


                        elementArray = dateArray[i].split("%");
                        Log.d("array", Arrays.toString(elementArray));
                        elementMode = elementArray[0];


                        if (elementArray[0].equals("NOTES") || elementArray[0].equals("ANNOUNCEMENTS")) {
                            words.add(new newsfeedPublic(elementArray[1], elementArray[2], elementArray[3], elementArray[4],
                                    elementArray[5], elementArray[6], elementArray[7]
                                    , elementArray[8], elementArray[0]));
                        } else {
                            words.add(new newsfeedPublic(elementArray[1], elementArray[2], elementArray[3], elementArray[4],
                                    elementArray[5], elementArray[6], elementArray[7]
                                    , elementArray[0]));
                        }


                    }






                    Log.v("appendedchampionDate",appendedChampionDate);

                    mProgress.hide();
                    EventList();

                }


                }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();

            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Newsfeed.REQUEST_AUTHORIZATION);
                } else {


                    end = true;

                    if(mode.equals("announcementViewer")) {
                        mode = "notesViewer";

                        getResultsFromApi();
                    }
                    else if(mode.equals("notesViewer")) {
                        mode = "eventViewer";

                        getResultsFromApi();
                    }  else if(mode.equals("eventViewer"))
                    {


                        String dateRetrieved = words.toString();
                        Log.v("trimmedString", dateRetrieved);
                        String trimmedString = dateRetrieved.replace("[","");
                        trimmedString = trimmedString.replace("]","");
                        Log.v("trimmedString", trimmedString);
                        trimmedString = trimmedString.replace(" NOTES","NOTES");
                        trimmedString = trimmedString.replace(" ANNOUNCEMENTS","ANNOUNCEMENTS");
                        trimmedString = trimmedString.replace(" EVENTS","EVENTS");
                        Log.v("trimmedString", trimmedString);


                        String dateArray[] = trimmedString.split(",");
                        String elementArray[];
                        String elementMode;



                        int i;

                        int latestIndex = 0;
                        String championDate = "date";
                        String descendingDates = "default";
                        String transferArray[] = {"hello","hello"};
                        String appendedChampionDate = ".";
                        Log.v("dateArray.length", String.valueOf(dateArray.length));
                        words.clear();
                        for(i=0;i<dateArray.length;i++) {

                            String latestDate;
//
                            elementArray = dateArray[i].split("%");
                            elementMode = elementArray[0];

                            Log.v("elementArray[0]", elementArray[0]);

                            if (elementMode.contains("ANNOUNCEMENTS") || elementMode.contains("NOTES")) {
                                latestDate = elementArray[7];

                            } else if (elementMode.contains("EVENTS")) {
                                latestDate = elementArray[3];
                            } else {
                                break;
                            }
//


                            int k = i;

                            for (k = k; k < dateArray.length; k++) {

                                String latestDataArray[] = latestDate.split("/");

                                int latestDateDay = Integer.parseInt(latestDataArray[0]);
                                int latestDateMonth = Integer.parseInt(latestDataArray[1]);
                                int latestDateYear = Integer.parseInt(latestDataArray[2]);


                                elementArray = dateArray[k].split("%");
                                elementMode = elementArray[0];
                                String challengeDate = "o";
                                if (elementMode.equals("ANNOUNCEMENTS") || elementMode.equals("NOTES")) {
                                    challengeDate = elementArray[7];

                                } else if (elementMode.equals("EVENTS")) {
                                    challengeDate = elementArray[3];
                                } else {
                                    break;
                                }

                                String challengeDateArray[] = challengeDate.split("/");
                                Log.v("k", String.valueOf(k));
                                Log.v("latestDate", latestDate);
                                Log.v("challengeDate", challengeDate);
//                            Log.v("challengeDateArray", String.valueOf(challengeDate));
                                int challengeDateDay = Integer.parseInt(challengeDateArray[0]);
                                int challengeDateMonth = Integer.parseInt(challengeDateArray[1]);
                                int challengeDateYear = Integer.parseInt(challengeDateArray[2]);

                                boolean lastestDayIsPast = false;

                                if (latestDateYear < challengeDateYear) {
                                    lastestDayIsPast = true;
                                } else if (latestDateMonth < challengeDateMonth) {
                                    if (latestDateYear <= challengeDateYear) {
                                        lastestDayIsPast = true;
                                    }
                                } else if (latestDateDay < challengeDateDay) {
                                    if (latestDateMonth <= challengeDateMonth) {
                                        if (latestDateYear <= challengeDateYear) {
                                            lastestDayIsPast = true;

                                        }
                                    }
                                }
//                            Log.v("lastestDayIsPast", String.valueOf(lastestDayIsPast));
                                if (lastestDayIsPast) {
                                    championDate = challengeDate;
                                    latestDate = challengeDate;
                                    latestIndex = k;

                                }

                                lastestDayIsPast = false;


                            }
                            appendedChampionDate = appendedChampionDate + "  - \n " + latestDate;


                            transferArray[0] = dateArray[i];
                            dateArray[i] = dateArray[latestIndex];

                            dateArray[latestIndex] = transferArray[0];

                            Log.v("championDate", latestDate + latestIndex);


                            elementArray = dateArray[i].split("%");
                            Log.d("array", Arrays.toString(elementArray));
                            elementMode = elementArray[0];


                            if (elementArray[0].equals("NOTES") || elementArray[0].equals("ANNOUNCEMENTS")) {
                                words.add(new newsfeedPublic(elementArray[1], elementArray[2], elementArray[3], elementArray[4],
                                        elementArray[5], elementArray[6], elementArray[7]
                                        , elementArray[8], elementArray[0]));
                            } else {
                                words.add(new newsfeedPublic(elementArray[1], elementArray[2], elementArray[3], elementArray[4],
                                        elementArray[5], elementArray[6], elementArray[7]
                                        , elementArray[0]));
                            }


                        }






                        Log.v("appendedchampionDate",appendedChampionDate);

                        mProgress.hide();
                        EventList();

                    }


                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }



    public void onClick2(View v) {

        getResultsFromApi();


    }












    public void loadData(){
        SharedPreferences mPrefs = getSharedPreferences("label", 0);


        sDepartment =  mPrefs.getString("Department", "unknown");
        String semester = mPrefs.getString("Semester", "default_value_if_variable_not_found");

        if (semester.equals("1")) {
            sSemester = "First Semester";
        } else if (semester.equals("2")) {
            sSemester = "Second Semester";
        } else if (semester.equals("3")) {
            sSemester = "Third Semester";
        } else if (semester.equals("4")) {
            sSemester = "Fourth Semester";
        } else if (semester.equals("5")) {
            sSemester = "Fifth Semester";
        } else if (semester.equals("6")) {
            sSemester = "Sixth and Above Semesters";
        }



        //tableDetails
        tableNo = mPrefs.getInt("tableNo", 1);
        String AnnSheetId = mPrefs.getString("AnnSheetId", "1OKiX0QWm2VerdWhuLPF1NIoTEOXHpBFo9qNPgu9HH7Y");
        gSavedAnnSheetId = AnnSheetId;



    }

    public void removeIndexFromArray(String array[],int index){
        array[index] = "remove";
        int i;
        for(i=0;i< array.length;i++)
        {
            if(array[i].contains("remove"))
            {

            }else
            {
                array[i] = array[i];
            }
        }
        Log.d("array", Arrays.toString(array));
        globalTransferArray = array;

        return;




    }

    public void timetableDataConversion(int localDayIndex)
    {

        if(dayIndex==7)
        {
            dayIndex = 1;
            localDayIndex = 1;
        }
        String timetable = "  10:10 -  10:55<time>  10:55 -  11:40<time>  11:40 -  12:25<time>  12:25 -  01:10<type6280>" +
                "CA<subjects>BS<subjects>ME<subjects>BC-II<subjects><type6280>" +
                "MTP<subjects>CA<subjects>BS<subjects>ME<subjects><type6280>" +
                "LRF<subjects>ME<subjects>CA<subjects>BS<subjects><type6280>" +
                "BC-II<subjects>LRF<subjects>MTP<subjects>CA<subjects><type6280>" +
                "BS<subjects>BC-II<subjects>LRF<subjects>MTP<subjects><type6280>" +
                "ME<subjects>BC-II<subjects>MTP<subjects>LRF<subjects><type6280>";

        String dataSplit[] =  timetable.split("<type6280>");
        String timeSlots[] = dataSplit[0].split("<time>");

        TextView timeSlot0 = (TextView) findViewById(R.id.timeSlot0);
        timeSlot0.setText(timeSlots[0]);

        TextView timeSlot1 = (TextView) findViewById(R.id.timeSlot1);
        timeSlot1.setText(timeSlots[1]);

        TextView timeSlot2 = (TextView) findViewById(R.id.timeSlot2);
        timeSlot2.setText(timeSlots[2]);

        TextView timeSlot3 = (TextView) findViewById(R.id.timeSlot3);
        timeSlot3.setText(timeSlots[3]);


        String dayText = "default";

        if(localDayIndex == 1)
            dayText = "Monday";
        else if(localDayIndex == 2)
            dayText = "Tuesday";
        else if(localDayIndex == 3)
            dayText = "Wednesday";
        else if(localDayIndex == 4)
            dayText = "Thursday";
        else if(localDayIndex == 5)
            dayText = "Friday";
        else if(localDayIndex == 6)
            dayText = "Saturday";
        

        TextView dayOfTheWeek = (TextView) findViewById(R.id.dayOfTheWeek);
        dayOfTheWeek.setText(dayText);



        String subjectSlots[] = dataSplit[localDayIndex].split("<subjects>");
        TextView subjectSlot0 = (TextView) findViewById(R.id.subjectSlot0);
        subjectSlot0.setText(subjectSlots[0]);

        TextView subjectSlot1 = (TextView) findViewById(R.id.subjectSlot1);
        subjectSlot1.setText(subjectSlots[1]);

        TextView subjectSlot2 = (TextView) findViewById(R.id.subjectSlot2);
        subjectSlot2.setText(subjectSlots[2]);

        TextView subjectSlot3 = (TextView) findViewById(R.id.subjectSlot3);
        subjectSlot3.setText(subjectSlots[3]);








    }

    public void EventList(){

        if(mode.equals("announcementViewer")) {



            newsfeedAdapter adapter = new newsfeedAdapter(this, words);


            ListView listView = (ListView) findViewById(R.id.list);


            listView.setAdapter(adapter);

            if (end) {

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        newsfeedPublic word = words.get(position);

                        //Get on clicked data
                        String exTitle = word.getMiwokTranslation();  //Title
                        String exDesc = word.getDefaultTranslation();  //Description
                        String exPublishDate = word.getPublishDate(); // Cataegories
                        String exEventDate = word.getEventDate();// Publisher Id
                        String exFullName = word.getLastDateofRegistration(); //full name
                        String exFees = word.getEntryFees(); // Unique Id
                        String exLastDateofRegistation = word.getfullName(); // Publish Date
                        String exFileAttachment = word.getFileAttachment();



                        //Save Data
                        SharedPreferences mPrefs = getSharedPreferences("label", 0);
                        SharedPreferences.Editor mEditor = mPrefs.edit();
                        mEditor.putString("title", exTitle).commit();
                        mEditor.putString("desc", exDesc).commit();
                        mEditor.putString("publishDate", exPublishDate).commit();
                        mEditor.putString("eventDate", exEventDate).commit();
                        mEditor.putString("lastDateOfRegistration", exLastDateofRegistation).commit();
                        mEditor.putString("fees", exFees).commit();
                        mEditor.putString("fullName", exFullName).commit();
                        mEditor.putString("fileAttachment", exFileAttachment).commit();


                        Intent selectIntent = new Intent(Newsfeed.this, t_Detailed_Announcement.class);
                        startActivity(selectIntent);


                    }


                });
            }
        }// End of announcementViewer mode for EventList

        if(mode.equals("notesViewer")) {



            newsfeedAdapter adapter = new newsfeedAdapter(this, words);


            ListView listView = (ListView) findViewById(R.id.list);


            listView.setAdapter(adapter);

            if(end) {

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        newsfeedPublic word = words.get(position);

                        //Get on clicked data
                        String exTitle = word.getMiwokTranslation();  //Title
                        String exDesc = word.getDefaultTranslation();  //Description
                        String exPublishDate = word.getPublishDate(); // Cataegories
                        String exEventDate = word.getEventDate();// Publisher Id
                        String exFullName = word.getLastDateofRegistration(); //full name
                        String exFees = word.getEntryFees(); // Unique Id
                        String exLastDateofRegistation = word.getfullName(); // Publish Date
                        String exFileAttachment = word.getFileAttachment();




                        //Save Data
                        SharedPreferences mPrefs = getSharedPreferences("label", 0);
                        SharedPreferences.Editor mEditor = mPrefs.edit();
                        mEditor.putString("title", exTitle).commit();
                        mEditor.putString("desc", exDesc).commit();
                        mEditor.putString("fullName", exFullName).commit();
                        mEditor.putString("fileAttachment", exFileAttachment).commit();





                        Intent selectIntent = new Intent(Newsfeed.this,t_Detailed_Notes.class);
                        startActivity(selectIntent);






                    }


                });
            }


        }else if(mode.equals("eventViewer")){

            newsfeedAdapter adapter = new newsfeedAdapter(this, words);


            ListView listView = (ListView) findViewById(R.id.list);

            listView.setAdapter(adapter);

            if(end) {

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        newsfeedPublic word = words.get(position);

                        //Get on clicked data
                        String exMode = word.getMode();
                        if(exMode.contains("EVENTS")) {
                            String exTitle = word.getMiwokTranslation();
                            String exDesc = word.getDefaultTranslation();
                            String exPublishDate = word.getPublishDate();
                            String exEventDate = word.getEventDate();
                            String exLastDateofRegistation = word.getLastDateofRegistration();
                            String exFees = word.getEntryFees();
                            String exFullName = word.getfullName();


                            //Save Data
                            SharedPreferences mPrefs = getSharedPreferences("label", 0);
                            SharedPreferences.Editor mEditor = mPrefs.edit();
                            mEditor.putString("title", exTitle).commit();
                            mEditor.putString("desc", exDesc).commit();
                            mEditor.putString("publishDate", exPublishDate).commit();
                            mEditor.putString("eventDate", exEventDate).commit();
                            mEditor.putString("lastDateOfRegistration", exLastDateofRegistation).commit();
                            mEditor.putString("fees", exFees).commit();
                            mEditor.putString("fullName", exFullName).commit();


                            Intent selectIntent = new Intent(Newsfeed.this, DetailedEvent.class);
                            startActivity(selectIntent);


                        }else if(exMode.contains("ANNOUNCEMENTS"))
                        {
                            String exTitle = word.getMiwokTranslation();  //Title
                            String exDesc = word.getDefaultTranslation();  //Description
                            String exPublishDate = word.getPublishDate(); // Cataegories
                            String exEventDate = word.getEventDate();// Publisher Id
                            String exFullName = word.getLastDateofRegistration(); //full name
                            String exFees = word.getEntryFees(); // Unique Id
                            String exLastDateofRegistation = word.getfullName(); // Publish Date
                            String exFileAttachment = word.getFileAttachment();



                            //Save Data
                            SharedPreferences mPrefs = getSharedPreferences("label", 0);
                            SharedPreferences.Editor mEditor = mPrefs.edit();
                            mEditor.putString("title", exTitle).commit();
                            mEditor.putString("desc", exDesc).commit();
                            mEditor.putString("publishDate", exPublishDate).commit();
                            mEditor.putString("eventDate", exEventDate).commit();
                            mEditor.putString("lastDateOfRegistration", exLastDateofRegistation).commit();
                            mEditor.putString("fees", exFees).commit();
                            mEditor.putString("fullName", exFullName).commit();
                            mEditor.putString("fileAttachment", exFileAttachment).commit();


                            Intent selectIntent = new Intent(Newsfeed.this, t_Detailed_Announcement.class);
                            startActivity(selectIntent);

                        }else
                        {
                            String exTitle = word.getMiwokTranslation();  //Title
                            String exDesc = word.getDefaultTranslation();  //Description
                            String exPublishDate = word.getPublishDate(); // Cataegories
                            String exEventDate = word.getEventDate();// Publisher Id
                            String exFullName = word.getLastDateofRegistration(); //full name
                            String exFees = word.getEntryFees(); // Unique Id
                            String exLastDateofRegistation = word.getfullName(); // Publish Date
                            String exFileAttachment = word.getFileAttachment();




                            //Save Data
                            SharedPreferences mPrefs = getSharedPreferences("label", 0);
                            SharedPreferences.Editor mEditor = mPrefs.edit();
                            mEditor.putString("title", exTitle).commit();
                            mEditor.putString("desc", exDesc).commit();
                            mEditor.putString("fullName", exFullName).commit();
                            mEditor.putString("fileAttachment", exFileAttachment).commit();





                            Intent selectIntent = new Intent(Newsfeed.this,t_Detailed_Notes.class);
                            startActivity(selectIntent);


                        }


                    }


                });
            }

        }
    }

    public void onClickRightArrow(View v)
    {
        timetableDataConversion(++dayIndex);
    }
    public void onClickLeftArrow(View v)
    {
        timetableDataConversion(--dayIndex);
    }



    public void onClickAttendance(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,feedbackWriter.class);
        startActivity(selectIntent);


    }

    public void onClickAnnouncement(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,Newsfeed.class);
        startActivity(selectIntent);


    }

    public void onClickNotes(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,t_notes_Viewer.class);
        startActivity(selectIntent);


    }

    public void onClickEvents(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,EventViewer.class);
        startActivity(selectIntent);


    }

    public void onClickProfile(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,t_Teacher_Profile.class);
        startActivity(selectIntent);


    }

    public void onClickPlus(View v)
    {
        Intent selectIntent = new Intent(Newsfeed.this,t_Announcement_Writer.class);
        startActivity(selectIntent);


    }



    private void swipe() {

        TextView head2 = (TextView) findViewById(R.id.head2);
        TextView head1 = (TextView) findViewById(R.id.head);

        Button button1 = (Button) findViewById(R.id.Button1);
        Button button2 = (Button) findViewById(R.id.Button2);
        Button button3 = (Button) findViewById(R.id.Button3);



        if(a==0){

            Intent selectIntent = new Intent(Newsfeed.this,feedbackWriter.class);
            startActivity(selectIntent);

        }


        if(a==1) {

            Intent selectIntent = new Intent(Newsfeed.this,Newsfeed.class);
            startActivity(selectIntent);


        }

        if(a==2) {
            Intent selectIntent = new Intent(Newsfeed.this,t_notes_Viewer.class);
            startActivity(selectIntent);
        }

        if(a==3) {
            Intent selectIntent = new Intent(Newsfeed.this,EventViewer.class);
            startActivity(selectIntent);

        }

        if(a==4){
            Intent selectIntent = new Intent(Newsfeed.this,t_Teacher_Profile.class);
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
            profileImageView.setImageResource(R.drawable.profile);

        }
        if (a == 1) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements_grey);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.profile);
        }

        if (a == 2) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes_grey);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.profile);
        }

        if (a == 3) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events_grey);
            profileImageView.setImageResource(R.drawable.profile);
        }

        if (a == 4) {
            attendanceImageView.setImageResource(R.drawable.attendance);
            announcementImageView.setImageResource(R.drawable.announcements);
            notesImageView.setImageResource(R.drawable.notes);
            eventsImageView.setImageResource(R.drawable.events);
            profileImageView.setImageResource(R.drawable.profile_grey);
        }


    }

    private void deviceOffline()
    {
        LinearLayout activityLayout = (LinearLayout) findViewById(R.id.mLayout);
        activityLayout.setBackgroundResource(R.drawable.no_connection);
        RelativeLayout listviewer = (RelativeLayout) findViewById(R.id.content_viewer);
        listviewer.setVisibility(View.INVISIBLE);
    }
    private void deviceOnline()
    {
        LinearLayout activityLayout = (LinearLayout) findViewById(R.id.mLayout);
        activityLayout.setBackgroundResource(0);
        RelativeLayout listviewer = (RelativeLayout) findViewById(R.id.content_viewer);
        listviewer.setVisibility(View.VISIBLE);
    }






}