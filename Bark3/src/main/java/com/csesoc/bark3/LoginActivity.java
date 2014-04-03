package com.csesoc.bark3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button scanButton = (Button)findViewById(R.id.token_scan_button);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        Button submitButton = (Button)findViewById(R.id.token_submit_button);
        EditText tokenText = (EditText)findViewById(R.id.token_text);
        submitButton.setTag(tokenText);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tokenText = (EditText) v.getTag();
                String token = tokenText.getText().toString();
                if (isValidToken(token)) {
                    redirectToMain(token);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                String token = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (isValidToken(token)) {
                    redirectToMain(token);
                }
                Log.i("xZing", "contents: " + token + " format: " + format);              // Handle successful scan
            } else if(resultCode == RESULT_CANCELED) {              // Handle cancel
                Log.i("xZing", "Cancelled");
            }
        }
    }

    public void redirectToMain(String token) {
        Intent toMain = new Intent(this, MainActivity.class);
        toMain.putExtra("token", token);
        startActivity(toMain);
    }

    public boolean isValidToken(String token) {
        // some check presumably
        EditText tokenText = (EditText) findViewById(R.id.token_text);
        tokenText.setText(token);
        return true;
    }

}
