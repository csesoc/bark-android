package com.csesoc.bark3;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by john on 6/03/2014.
 */
public class RetrieveSiteData extends AsyncTask<String, Void, String> {
    private Context mContext;
    public RetrieveSiteData (Context context){
        mContext = context;
    }
    @Override
    protected String doInBackground(String... urls) {
        StringBuilder builder = new StringBuilder(16384);

        DefaultHttpClient client = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(urls[0]);

        try {
            HttpResponse execute = client.execute(httpGet);
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
        String cse = "";
        try{
        JSONObject myJson = new JSONObject(result);
        cse = myJson.optString("mem");
        } catch (JSONException e ) {

        }
        View colorbox = ((Activity)mContext).findViewById(R.id.color_box);
        if(cse.equals("true")) {
            colorbox.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            colorbox.setBackgroundResource(android.R.color.holo_red_light);
        }
    }

}
