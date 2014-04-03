package com.csesoc.bark3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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


public class MainActivity extends ActionBarActivity {
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("token")) {
            token = getIntent().getExtras().getString("token");
        } else {
            token = "";
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
                retrieveSiteData(zid);
            }
        });

        Button arcYesButton = (Button)findViewById(R.id.arc_yes_button);
        Button arcNoButton = (Button)findViewById(R.id.arc_no_button);

        arcYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HI", "YES");
            }
        });
        arcNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HI", "NO");
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
                    retrieveSiteData(zid);
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

    public void retrieveSiteData(String zid) {
        final EditText et;
        et = (EditText) findViewById(R.id.barcodeText);
        et.setText(zid);
        Context context = this;
        RetrieveSiteData task = new RetrieveSiteData(context);
        task.execute(zid, token);
        Log.d("zid", zid);
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
