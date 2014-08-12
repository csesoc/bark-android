package com.csesoc.bark3;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";
    private String token;
    private Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("token")) {
            token = getIntent().getExtras().getString("token");
        } else {
            token = "";
        }

        if (getIntent().hasExtra("name")) {
            ActionBar ab = getActionBar();
            ab.setTitle(getIntent().getExtras().getString("name"));
        }
        if (getIntent().hasExtra("location")) {
            ActionBar ab = getActionBar();
            ab.setSubtitle(getIntent().getExtras().getString("location"));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Button submitButton = (Button)findViewById(R.id.submit_zid);

        EditText barcodeText = (EditText)findViewById(R.id.barcodeText);
        submitButton.setTag(barcodeText);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                EditText barcodeText = (EditText) v.getTag();
                String zid = barcodeText.getText().toString();

                if (zid.length() != 7 && isValidBarcode(zid)) { // if it's a whole barcode, not just a zid
                    zid = zid.substring(2, 9);
                }
                updateStudent(zid);
            }
        });

        Button arcYesButton = (Button)findViewById(R.id.arc_yes_button);
        Button arcNoButton = (Button)findViewById(R.id.arc_no_button);

        arcYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateArc().execute(true);
            }
        });
        arcNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateArc().execute(false);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void scanNow(View view) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0)     {
            if(resultCode == RESULT_OK)         {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (isValidBarcode(contents)) {
                    String zid = contents.toString().substring(2, 9);
                    updateStudent(zid);
                }
                Log.i("xZing", "contents: "+contents+" format: "+format);              // Handle successful scan
            } else if(resultCode == RESULT_CANCELED)         {              // Handle cancel
                 Log.i("xZing", "Cancelled");
            }
        }
    }

    public boolean isValidBarcode(String barcode) {
        TextView cseText;
        cseText = (TextView) findViewById(R.id.cseText);
        if (barcode.length() != 14) {
            cseText.setText("Wrong length");
            return false;
        } else if (!barcode.substring(0,2).equals("X1")) {
            Log.i("xZing", barcode.substring(0,2));
            cseText.setText("Invalid Student Card");
        } else {
            cseText.setText("Valid Student Card");
        }
        return true;
    }

    public void updateStudent(String zid) {
        student = new Student();
        student.zid = zid;
        retrieveSiteData();
    }

    public void retrieveSiteData() {
        final EditText et;
        et = (EditText) findViewById(R.id.barcodeText);
        et.setText(student.zid);
        Context context = this;
        RetrieveSiteData task = new RetrieveSiteData(context);
        task.execute(student.zid, token);
        Log.d("zid", student.zid);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private class UpdateArc extends AsyncTask<Boolean, Void, Boolean> {
        private boolean arc;

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Boolean success;
            arc = params[0];
            StringBuilder builder = new StringBuilder(16384);

            JSONObject request = new JSONObject();
            try {
                request.put("token", token);
                request.put("action", "update_arc");
                request.put("is_arc", arc);
                request.put("zid", "z" + student.zid);

                DefaultHttpClient client = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(API_URL);
                StringEntity holder = new StringEntity(request.toString());

                httpPost.setEntity(holder);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse execute = client.execute(httpPost);

                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    builder.append(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String building = builder.toString();
            try {
                JSONObject result = new JSONObject(building);
                Log.d("arc_result", result.toString());
                if (result.getBoolean("success")) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // change button colour
                Toast.makeText(MainActivity.this, ((Boolean)arc).toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Arc update failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Student {
        public String zid = "";
        public String name = "";
        public String degree = "";
        public ArrayList<String> courses = new ArrayList<String>();
        public Integer numScans = 0;
        public Boolean arc = false;
        public boolean arcKnown = false;
        public Boolean cse = false;

        public Student(String zid, String name, String degree, ArrayList<String> courses, int numScans, Boolean arc, boolean cse) {
            this.zid = zid;
            this.name = name;
            this.degree = degree;
            this.courses = courses;
            this.numScans = numScans;
            this.arc = arc;
            if (arc != null) {
                this.arcKnown = true;
            }
            this.cse = cse;
        }
        public Student(String zid, JSONObject checkin) {
            this.zid = zid;
            try {
                this.name = checkin.getString("name");
                this.degree = checkin.getString("degree");
                JSONArray jCourses = checkin.getJSONArray("courses");
                for (int i=0; i<jCourses.length(); i++) {
                    String course = jCourses.getString(i);
                    this.courses.add(course);
                }
                this.numScans = checkin.getInt("num_scans");
                this.cse = checkin.getBoolean("is_cse");
            } catch (JSONException e) {
                e.printStackTrace();;
            }
            // set arc to true or false, or null if it's not true or false.
            try {
                this.arc = checkin.getBoolean("is_arc");
                this.arcKnown = true;
            } catch (JSONException e) {
                this.arc = null;
                this.arcKnown = false;
            }
        }
        public Student(Student student) {
            this.zid = student.zid;
            this.name = student.name;
            this.degree = student.degree;
            this.courses = student.courses;
            this.numScans = student.numScans;
            this.arc = student.arc;
            this.arcKnown = student.arcKnown;
            this.cse = student.cse;
        }
        public Student() {}

        @Override
        public String toString() {
            String student = this.name + " - " + this.zid;
            student += "\n";
            student += this.degree;
            student += "\n";
            for (String s : courses) {
                student += s;
                if (courses.indexOf(s) != courses.size() - 1) {
                    student += ", ";
                }
            }
            return student;
        }

    }

    public class RetrieveSiteData extends AsyncTask<String, Void, String> {
        private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";
        private Context mContext;
        public RetrieveSiteData (Context context){
            mContext = context;
        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder builder = new StringBuilder(16384);

            String zid = params[0];
            String token = params[1];

            try {
                JSONObject request = new JSONObject();
                request.put("token", token);
                request.put("action", "check_in");
                request.put("zid", "z" + zid);
                request.put("max_scans", 2);

                DefaultHttpClient client = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(API_URL);
                StringEntity holder = new StringEntity(request.toString());

                httpPost.setEntity(holder);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse execute = client.execute(httpPost);


                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    builder.append(s);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject resultJson = new JSONObject(result);
                student = new Student(student.zid, resultJson);

                final TextView cseText = (TextView) ((MainActivity)mContext).findViewById(R.id.cseText);
                final TextView cseMember = (TextView) ((Activity)mContext).findViewById(R.id.csesoc_text);
                Handler handler = new Handler();

                Runnable revertColors = new Runnable() {
                    @Override
                    public void run() {
                        cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));
                        cseMember.setTextColor(mContext.getResources().getColor(android.R.color.black));

                        cseText.setText(R.string.default_cse_text);
                    }
                };

                if (resultJson.getBoolean("success")) {

                    if (student.cse) {
                        cseMember.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));
                        cseMember.setTextColor(mContext.getResources().getColor(android.R.color.white));

                        final MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Settings.System.DEFAULT_NOTIFICATION_URI);
                        mediaPlayer.start();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mediaPlayer.stop();
                            }
                        }, 3000);
                    } else {
                        cseMember.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                        cseMember.setTextColor(mContext.getResources().getColor(android.R.color.white));

                        final MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
                        mediaPlayer.start();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mediaPlayer.stop();
                            }
                        }, 3000);
                    }

                    if (resultJson.get("is_arc") == null) { // wait for someone to press yes or no in arc buttons
                        handler.postDelayed(revertColors, 3000);
                    } else { // wait 3 seconds
                        handler.postDelayed(revertColors, 3000);
                    }

                    TextView studentInfo = (TextView) ((Activity)mContext).findViewById(R.id.student_info);

                    String studentInfoText = student.toString();


                    studentInfo.setText(studentInfoText);

                } else {
                    Log.e("JSON Error", resultJson.getString("error"));
                    cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));
                    cseMember.setTextColor(mContext.getResources().getColor(android.R.color.black));
                }
            } catch (Exception e ) {
                Log.e("Exception", e.getMessage());
            }
        }

    }


}
