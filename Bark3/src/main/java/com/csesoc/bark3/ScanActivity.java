package com.csesoc.bark3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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


public class ScanActivity extends AppCompatActivity {
    static final String INTENT_TOKEN = "token";
    static final String INTENT_NAME = "name";
    static final String INTENT_LOCATION= "location";
    static final String INTENT_START_TIME = "startTime";
    static final String INTENT_END_TIME = "endTime";
    static final String INTENT_RUNNING = "running";

    private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";
    private String token;
    private Student student;
    private int maxScans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (getIntent().hasExtra(INTENT_TOKEN)) {
            token = getIntent().getExtras().getString(INTENT_TOKEN);
        } else {
            token = "";
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null && getIntent().hasExtra(INTENT_NAME)) {
            ab.setTitle(getIntent().getStringExtra(INTENT_NAME));
        }
        if (ab != null && getIntent().hasExtra(INTENT_LOCATION)) {
            ab.setSubtitle(getIntent().getStringExtra(INTENT_LOCATION));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Button scanButton = (Button) findViewById(R.id.zid_scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        final EditText barcodeText = (EditText) findViewById(R.id.barcode_text);
        barcodeText.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER);
        EditText.OnEditorActionListener submitListener = new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EditText barcodeText = (EditText) v;
                String zid = barcodeText.getText().toString();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (zid.length() != 7 && isValidBarcode(zid)) { // if it's a whole barcode, not just a zid
                    zid = zid.substring(2, 9);
                }
                updateStudent(zid);
                barcodeText.setText("");

                return true;
            }
        };
        barcodeText.setOnEditorActionListener(submitListener);

        Button arcYesButton = (Button) findViewById(R.id.arc_yes_button);
        Button arcNoButton = (Button) findViewById(R.id.arc_no_button);

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


        final Spinner maxScansSpinner = (Spinner) findViewById(R.id.max_scans);
        Integer[] items = new Integer[]{1, 2, 3, 4};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, R.layout.max_scans_spinner_item, items);
        maxScansSpinner.setAdapter(adapter);

        maxScans = 1;
        maxScansSpinner.setSelection(0);

        maxScansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                maxScans = position + 1; // 0-based position, 1-based maxScans.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (isValidBarcode(contents)) {
                    String zid = contents.substring(2, 9);
                    updateStudent(zid);
                }
                Log.i("xZing", "contents: " + contents + " format: " + format);              // Handle successful scan
            } else if (resultCode == RESULT_CANCELED) {              // Handle cancel
                Log.i("xZing", "Cancelled");
            }
        }
    }

    public boolean isValidBarcode(String barcode) {
        TextView cseText;
        cseText = (TextView) findViewById(R.id.error_text);
        if (barcode.length() != 14) {
            cseText.setText("Wrong length");
            return false;
        } else if (!barcode.substring(0, 2).equals("X1")) {
            Log.i("xZing", barcode.substring(0, 2));
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
        et = (EditText) findViewById(R.id.barcode_text);
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
            return inflater.inflate(R.layout.fragment_main, container, false);
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
            TextView errorText = (TextView) findViewById(R.id.error_text);
            errorText.setVisibility(View.GONE);
            if (success) {
                // change the button colour
                student.arc = arc;
                Button yesButton = (Button) findViewById(R.id.arc_yes_button);
                Button noButton = (Button) findViewById(R.id.arc_no_button);
                TextView arcLabel = (TextView) findViewById(R.id.arc_text);
                if (arc) {
                    yesButton.setSelected(true);
                    noButton.setSelected(false);
                    arcLabel.setBackgroundResource(R.color.positive);
                } else {
                    yesButton.setSelected(false);
                    noButton.setSelected(true);
                    arcLabel.setBackgroundResource(R.color.negative);
                }
            } else {
                errorText.setText("Arc update failed");
                errorText.setVisibility(View.VISIBLE);
            }

            // refocus the barcode box
            EditText barcodeText = (EditText) ScanActivity.this.findViewById(R.id.barcode_text);
            barcodeText.requestFocus();
        }
    }

    private class Student {
        public String name = "";

        private String zid = "";
        private String degree = "";
        private ArrayList<String> courses = new ArrayList<String>();
        private Integer numScans = 0;
        private Boolean arc = false;
        private boolean arcKnown = false;
        private Boolean cse = false;

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

        private Student(String zid, JSONObject checkin) {
            this.zid = zid;
            try {
                this.name = checkin.getString("name");
                this.degree = checkin.getString("degree");
                JSONArray jCourses = checkin.getJSONArray("courses");
                for (int i = 0; i < jCourses.length(); i++) {
                    String course = jCourses.getString(i);
                    this.courses.add(course);
                }
                this.numScans = checkin.getInt("num_scans");
                this.cse = checkin.getBoolean("is_cse");
            } catch (JSONException e) {
                e.printStackTrace();
                ;
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

        Student() {
        }

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

    private class RetrieveSiteData extends AsyncTask<String, String, String> {
        private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";
        private Context mContext;

        private RetrieveSiteData(Context context) {
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
                request.put("max_scans", maxScans);

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
            Log.d("zid", result);
            try {
                JSONObject resultJson = new JSONObject(result);
                student = new Student(student.zid, resultJson);

                Button yesButton = (Button) findViewById(R.id.arc_yes_button);
                Button noButton = (Button) findViewById(R.id.arc_no_button);
                TextView arcLabel = (TextView) findViewById(R.id.arc_text);
                if (student.arcKnown) {
                    // change arc button colour if arc is defined
                    if (student.arc) {
                        yesButton.setSelected(true);
                        noButton.setSelected(false);
                        arcLabel.setBackgroundResource(R.color.positive);
                    } else {
                        yesButton.setSelected(false);
                        noButton.setSelected(true);
                        arcLabel.setBackgroundResource(R.color.negative);
                    }
                } else {
                    yesButton.setSelected(false);
                    noButton.setSelected(false);
                    arcLabel.setBackgroundResource(R.color.light_grey);
                }

                final TextView errorText = (TextView) findViewById(R.id.error_text);
                errorText.setVisibility(View.GONE);

                final TextView cseMember = (TextView) findViewById(R.id.csesoc_text);

                if (resultJson.getBoolean("success")) {
                    // set the CSE member colour and play noise
                    if (student.cse) {
                        cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.positive));
                        if (student.arcKnown) {
                            final MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.success);
                            mPlayer.start();
                        } else {
                            final MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.no_arc);
                            mPlayer.start();
                        }
                    } else {
                        cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.negative));
                        final MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.warning);
                        mPlayer.start();
                    }

                    TextView studentName = (TextView) ((Activity) mContext).findViewById(R.id.student_name);
                    studentName.setText(student.name);

                    TextView studentZid = (TextView) ((Activity) mContext).findViewById(R.id.student_zid);
                    studentZid.setText("z" + student.zid);

                    TextView studentDegree = (TextView) ((Activity) mContext).findViewById(R.id.student_degree);
                    studentDegree.setText(student.degree);

                    TextView studentScans = (TextView) ((Activity) mContext).findViewById(R.id.student_scans);
                    studentScans.setText(student.numScans.toString());

                    TextView studentCourses = (TextView) ((Activity) mContext).findViewById(R.id.student_courses);
                    String courses = "";
                    for (String s : student.courses) {
                        if (s.matches("COMP[0-9]{4}")) {
                            courses += "CS" + s.substring(4);
                        } else {
                            courses += s;
                        }
                        if (student.courses.indexOf(s) != student.courses.size() - 1) {
                            courses += ", ";
                        }
                    }
                    if (courses.equals("")) {
                        courses = "None";
                    }
                    studentCourses.setText(courses);

                } else {
                    // play error sound
                    final MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.warning);
                    mPlayer.start();

                    // show an error
                    errorText.setText(resultJson.getString("error"));
                    errorText.setVisibility(View.VISIBLE);

                    // reset everything
                    cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));

                    yesButton.setSelected(false);
                    noButton.setSelected(false);
                    arcLabel.setBackgroundResource(R.color.light_grey);

                    TextView studentName = (TextView) findViewById(R.id.student_name);
                    studentName.setText("");

                    TextView studentZid = (TextView) findViewById(R.id.student_zid);
                    studentZid.setText("");

                    TextView studentDegree = (TextView) findViewById(R.id.student_degree);
                    studentDegree.setText("");

                    TextView studentScans = (TextView) findViewById(R.id.student_scans);
                    studentScans.setText("");

                    TextView studentCourses = (TextView) findViewById(R.id.student_courses);
                    studentCourses.setText("");

                    student = null;
                }

                // refocus the barcode box
                EditText barcodeText = (EditText) ((Activity) mContext).findViewById(R.id.barcode_text);
                barcodeText.requestFocus();
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null) {
                    Log.e("Exception", msg);
                } else {
                    Log.e("Exception", "<null message>");
                }
            }
        }

    }


}
