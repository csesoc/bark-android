package com.csesoc.bark3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;


public class LoginActivity extends Activity {
    private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";

    private LinkedList<Token> tokens = new LinkedList<Token>();
    private TokenAdapter adapter;

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

        Button typeButton = (Button)findViewById(R.id.token_submit_button);
        EditText tokenText = (EditText)findViewById(R.id.token_text);
        tokenText.setImeActionLabel("Submit", KeyEvent.KEYCODE_ENTER);

        EditText.OnEditorActionListener submitListener = new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if (actionId == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {

                        EditText tokenText = (EditText) v;
                        String token = tokenText.getText().toString();
                        new CheckToken().execute(token);

                        return true;
                    }
                    return false;
                }
                if (actionId == KeyEvent.KEYCODE_ENTER) {

                    EditText tokenText = (EditText) v;
                    String token = tokenText.getText().toString();
                    new CheckToken().execute(token);

                    return true;
                }
                return true;
            }
        };
        tokenText.setOnEditorActionListener(submitListener);

        typeButton.setTag(tokenText);

        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText tokenText = (EditText) v.getTag();
                TextView typeButton = (TextView) v;

                if (tokenText.getVisibility() == View.GONE) {
                    tokenText.setVisibility(View.VISIBLE);
                    typeButton.setText(R.string.hide_type_token);
                } else if (tokenText.getVisibility() == View.VISIBLE) {
                    tokenText.setVisibility(View.GONE);
                    typeButton.setText(R.string.show_type_token);
                }

            }
        });

        ListView tokenList = (ListView) findViewById(R.id.token_list);
        adapter = new TokenAdapter(this, tokens);
        tokenList.setAdapter(adapter);

        tokenList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Token item = tokens.get(position);

                if (item != null) {
                    Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
                    toMain.putExtra("token", item.token);
                    toMain.putExtra("name", item.name);
                    toMain.putExtra("location", item.location);
                    toMain.putExtra("startTime", item.startTime);
                    toMain.putExtra("endTime", item.endTime);
                    toMain.putExtra("running", item.running);
                    startActivity(toMain);
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

                Log.d("token", token);

                new CheckToken().execute(token);

                Log.i("xZing", "contents: " + token + " format: " + format);    // Handle successful scan
            } else if(resultCode == RESULT_CANCELED) {      // Handle cancel
                Log.i("xZing", "Cancelled");
            }
        }
    }

    private class CheckToken extends AsyncTask<String, Void, JSONObject> {
        private Context mContext;
        @Override
        protected JSONObject doInBackground(String... params) {
            Boolean success;
            String token = params[0];
            StringBuilder builder = new StringBuilder(16384);

            JSONObject request = new JSONObject();
            try {
                request.put("token", token);
                request.put("action", "get_event_info");

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

            JSONObject result = new JSONObject();
            String building = builder.toString();
            try {
                result = new JSONObject(building);
                result.put("token", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            boolean success = false;
            String token = "";
            String name = "Nothing";
            String location = "Nowhere";
            int startTime = 0;
            int endTime = 0;
            boolean running = false;
            try {
                success = result.getBoolean("success");
                token = result.getString("token");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) { // go to main activity on success, passing on the event info

                try {
                    name = result.getString("name");
                    location = result.getString("location");
                    startTime = result.getInt("start_time");
                    endTime = result.getInt("end_time");
                    running = result.getBoolean("running");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Token tokenObj = new Token(name, token, location, startTime, endTime, running);

                ListView tokenList = (ListView) LoginActivity.this.findViewById(R.id.token_list);
                tokens.add(0, tokenObj);
                adapter.notifyDataSetChanged();

            } else {
                EditText tokenText = (EditText) findViewById(R.id.token_text);
                tokenText.setText(token);
                try {
                    String error = result.getString("error");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Token {
        public String name;
        public String token;
        public String location;
        public int startTime;
        public int endTime;
        public boolean running;

        public Token(String name, String token, String location, int startTime, int endTime, boolean running) {
            this.name = name;
            this.token = token;
            this.location = location;
            this.startTime = startTime;
            this.endTime = endTime;
            this.running = running;
        }
    }

    public class TokenAdapter extends BaseAdapter {
        private Context context;
        LinkedList<Token> items;

        public TokenAdapter(Context context, LinkedList<Token> items) {
            super();
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Token getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return -1;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.token_list_item, null);
            }

            Token item = getItem(position);
            if (item!= null) {
                TextView nameView = (TextView) view.findViewById(R.id.token_name);
                if (nameView != null) {
                    nameView.setText(item.name);
                }
                TextView tokenView = (TextView) view.findViewById(R.id.token_location);
                if (tokenView != null) {
                    tokenView.setText(item.location);
                }
            }

            return view;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }

}
