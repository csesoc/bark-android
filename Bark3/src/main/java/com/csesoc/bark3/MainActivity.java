package com.csesoc.bark3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
                final EditText et;
                et = (EditText) findViewById(R.id.barcodeText);
                if (isValidBarcode(contents)) {
                    String zID = "z" + contents.toString().substring(2, 9);
                    et.setText(zID);
                    Context context = this;
                    RetrieveSiteData task = new RetrieveSiteData(context);
                    task.execute("http://jwis261.web.cse.unsw.edu.au/cseid.php?id="+zID);
                }
                Log.i("xZing", "contents: "+contents+" format: "+format);              // Handle successful scan
            } else if(resultCode == RESULT_CANCELED)         {              // Handle cancel
                 Log.i("xZing", "Cancelled");
            }
        }
    }

    public boolean isValidBarcode(String barcode) {
        TextView tv;
        tv = (TextView) findViewById(R.id.cseText);
        if (barcode.length() != 14) {
            tv.setText("Wrong length");
            return false;
        } else if (!barcode.substring(0,2).equals("X1")) {
            Log.i("xZing", barcode.substring(0,2));
            tv.setText("Invalid");
        };
        return true;
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


}
