package com.csesoc.bark3;

/**
 * Created by john on 6/03/2014.
 */
//public class RetrieveSiteData extends AsyncTask<String, Void, String> {
//    private static final String API_URL = "https://bark.csesoc.unsw.edu.au/api";
//    private Context mContext;
//    public RetrieveSiteData (Context context){
//        mContext = context;
//    }
//    @Override
//    protected String doInBackground(String... params) {
//        StringBuilder builder = new StringBuilder(16384);
//
//        String zid = params[0];
//        String token = params[1];
//
//        try {
//            JSONObject request = new JSONObject();
//            request.put("token", token);
//            request.put("action", "check_in");
//            request.put("zid", zid);
//
//            DefaultHttpClient client = new DefaultHttpClient();
//
//            HttpPost httpPost = new HttpPost(API_URL);
//            StringEntity holder = new StringEntity(request.toString());
//
//            httpPost.setEntity(holder);
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//
//            HttpResponse execute = client.execute(httpPost);
//
//
//            InputStream content = execute.getEntity().getContent();
//
//            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
//            String s = "";
//            while ((s = buffer.readLine()) != null) {
//                builder.append(s);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        return builder.toString();
//    }
//
//    @Override
//    protected void onPostExecute(String result) {
//        try {
//            JSONObject resultJson = new JSONObject(result);
//
//            final TextView cseText = (TextView) ((MainActivity)mContext).findViewById(R.id.cseText);
//            final TextView cseMember = (TextView) ((Activity)mContext).findViewById(R.id.csesoc_text);
//            Handler handler = new Handler();
//
//            Runnable revertColors = new Runnable() {
//                @Override
//                public void run() {
//                    cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));
//                    cseMember.setTextColor(mContext.getResources().getColor(android.R.color.black));
//
//                    cseText.setText(R.string.default_cse_text);
//                }
//            };
//
//            if (resultJson.getBoolean("success")) {
//
//                if (resultJson.getBoolean("is_cse")) {
//                    cseMember.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));
//                    cseMember.setTextColor(mContext.getResources().getColor(android.R.color.white));
//
//                    final MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Settings.System.DEFAULT_NOTIFICATION_URI);
//                    mediaPlayer.start();
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mediaPlayer.stop();
//                        }
//                    }, 3000);
//                } else {
//                    cseMember.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_red_light));
//                    cseMember.setTextColor(mContext.getResources().getColor(android.R.color.white));
//
//                    final MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Settings.System.DEFAULT_ALARM_ALERT_URI);
//                    mediaPlayer.start();
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mediaPlayer.stop();
//                        }
//                    }, 3000);
//                }
//
//                if (resultJson.get("is_arc") == null) { // wait for someone to press yes or no in arc buttons
//                    handler.postDelayed(revertColors, 3000);
//                } else { // wait 3 seconds
//                    handler.postDelayed(revertColors, 3000);
//                }
//
//                TextView studentInfo = (TextView) ((Activity)mContext).findViewById(R.id.student_info);
//
//                String studentInfoText = resultJson.getString("name") +
//                        "\n" + resultJson.getString("degree") +
//                        "\n" + resultJson.getJSONArray("courses").join(", ");
//
//
//                studentInfo.setText(studentInfoText);
//
//            } else {
//                Log.e("JSON Error", resultJson.getString("error"));
//                cseMember.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));
//                cseMember.setTextColor(mContext.getResources().getColor(android.R.color.black));
//            }
//        } catch (Exception e ) {
//            Log.e("Exception", e.getMessage());
//        }
//    }
//
//}
