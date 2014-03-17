package com.csesoc.bark3;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

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

        Log.d("url", urls[0]);

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
            Log.e("JSONException", e.getMessage());
        }
//        View colorbox = ((Activity)mContext).findViewById(R.id.container);
        ActionBar ab = ((Activity)mContext).getActionBar();
        TextView cseMember = (TextView) ((Activity)mContext).findViewById(R.id.csesoc_text);

        String mSpeakString;

        if(cse.equals("true")) {
//            mSpeakString = "Valid C.S.E.Soc Member";
            mSpeakString = "Valid SeeEssEeSock Member";
            cseMember.setBackgroundColor(((Activity)mContext).getResources().getColor(android.R.color.holo_green_light));
            cseMember.setTextColor(((Activity) mContext).getResources().getColor(android.R.color.white));

        } else if (cse.equals("false")) {
            mSpeakString = "This Person Is Not In SeeEssEeSock";
            cseMember.setBackgroundColor(((Activity) mContext).getResources().getColor(android.R.color.holo_red_light));
            cseMember.setTextColor(((Activity)mContext).getResources().getColor(android.R.color.white));
        } else {
            mSpeakString = "Network Error. Please try again.";
            cseMember.setBackgroundColor(((Activity) mContext).getResources().getColor(R.color.light_grey));
            cseMember.setTextColor(((Activity)mContext).getResources().getColor(android.R.color.black));
        }
        final String speakString = mSpeakString;
        ((MainActivity)mContext).mTts = new TextToSpeech((Activity)mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
              ((MainActivity)mContext).mTts.setLanguage(Locale.US);
                ((MainActivity)mContext).mTts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

}
