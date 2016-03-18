package com.smithahm.ratetheinstructor;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.audiofx.LoudnessEnhancer;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DetailActivity extends ActionBarActivity {

    InstructorDatabaseAdapter instDatabase;

    private long id = 0;
    private String comments = "";
    AndroidHttpClient httpclient;
    Context context;
    private TextView firstName,secondName,oFFice,pHone,eMail,average,tOtal;
    private Button ratIng, addComment;
    private EditText editcomment;
    ListView instructorList;
    ArrayList<String> instructorArrayList = new ArrayList<String>();
    ArrayAdapter<String> instructorAdapter;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        id = intent.getLongExtra(MainActivity.EXTRA_MESSAGE, 0);
        id = id + 1;
        context = this;
        instDatabase = new InstructorDatabaseAdapter(this);

        instructorList= (ListView)findViewById(R.id.commentList);
        instructorAdapter = new ArrayAdapter<String>(this, R.layout.activity_test, instructorArrayList);
        instructorList.setAdapter(instructorAdapter);

        firstName = (TextView)this.findViewById(R.id.fname);
        secondName = (TextView)this.findViewById(R.id.sname);
        oFFice = (TextView)this.findViewById(R.id.office);
        pHone = (TextView)this.findViewById(R.id.phone);
        eMail = (TextView)this.findViewById(R.id.email);
        average = (TextView)this.findViewById(R.id.avg);
        tOtal = (TextView)this.findViewById(R.id.total);
        ratIng = (Button)this.findViewById(R.id.rating);
        addListenerOnRatingBar();
        editcomment = (EditText)findViewById(R.id.editcomment);
        editcomment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });
    }


    public void onResume() {
        super.onResume();
        String userAgent = null;
        httpclient = AndroidHttpClient.newInstance(userAgent);

        runInstructorDetailCode();

        HttpCommentTask commentTask = new HttpCommentTask();
        String commentUrl = "http://bismarck.sdsu.edu/rateme/comments/"+id;
        commentTask.execute(commentUrl);
    }

    public void runInstructorDetailCode(){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://bismarck.sdsu.edu/rateme/instructor/" + id;

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rate = response.getJSONObject("rating");
                    String avg = rate.getString("average");
                    String total = rate.getString("totalRatings");
                    firstName.setText(response.getString("firstName"));
                    secondName.setText(response.getString("lastName"));
                    oFFice.setText(response.getString("office"));
                    pHone.setText(response.getString("phone"));
                    eMail.setText(response.getString("email"));
                    average.setText(avg + " out of 5");
                    tOtal.setText(total);
                    Log.i("rew", "Inside Volley network code");
                    instDatabase.insertData(response.getString("firstName"), response.getString("lastName"), response.getString("office"), response.getString("phone"), response.getString("email"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("rew", "error");
            }
        });
        queue.add(getRequest);
    }

    public void addListenerOnRatingBar() {
        ratIng = (Button)this.findViewById(R.id.rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratIng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(ratingBar.getRating() == 0.0){
                   Toast.makeText(DetailActivity.this,"Select the rating stars", Toast.LENGTH_SHORT).show();
                   return;
               }
                else{
                   Integer rate = Math.round(ratingBar.getRating());
                   String userAgent = null;
                   httpclient = AndroidHttpClient.newInstance(userAgent);

                   HttpPostTask task = new HttpPostTask();
                   String url = "http://bismarck.sdsu.edu/rateme/rating/"+id +"/"+rate;
                   task.execute(url);
                   Toast.makeText(DetailActivity.this,rate.toString(),Toast.LENGTH_SHORT).show();
               }
            }
        });


    }

    public void runNetworkCode(View button) {
        addComment = (Button)this.findViewById(R.id.submit);
        editcomment = (EditText)this.findViewById(R.id.editcomment);

        if(editcomment.getText().toString().matches("")){
             Toast.makeText(DetailActivity.this, "You did not enter a comment", Toast.LENGTH_SHORT).show();
             return;
           }
       else{
            String userAgent = null;
            httpclient = AndroidHttpClient.newInstance(userAgent);
            comments=editcomment.getText().toString();

            HttpCommentPostTask comment= new HttpCommentPostTask();
            String commentUrl = "http://bismarck.sdsu.edu/rateme/comment/"+id;
            comment.execute(commentUrl);
       }
    }


    public void onPause() {
        super.onPause();
        httpclient.close();
        instDatabase.close();
    }


    class HttpPostTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog = new ProgressDialog(context);
            dialog.setTitle("Submitting rating");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpPost postMethod = new HttpPost(urls[0]);
            postMethod.setHeader("Content-Type", "application/json;charset=UTF-8");

            try {
                HttpResponse responseBody = httpclient.execute(postMethod);
            } catch (Throwable t) {
                Log.i("rew", t.toString());
            }
            return null;
        }

        public void onPostExecute(String jsonString) {
            dialog.dismiss();
            runInstructorDetailCode();
            ratingBar.setRating(0.0f);
        }
    }

    class HttpCommentTask extends AsyncTask<String, Void, String> {
        Utility u = new Utility();
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Fetching data");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                ResponseHandler<String> responseHandler =
                        new BasicResponseHandler();
                HttpGet getMethod = new HttpGet(urls[0]);
                String responseBody = httpclient.execute(getMethod,
                        responseHandler);
                return responseBody;
            } catch (Throwable t) {
                Log.i("rew", "did not work", t);
            }
            return null;

        }

        public void onPostExecute(String jsonString) {
            try {
                JSONArray data = new JSONArray(jsonString);
                for (int i = 0; i < data.length(); i++) {
                    JSONObject firstPerson = (JSONObject) data.get(i);
                    String text = firstPerson.getString("text");
                    String dateString = firstPerson.getString("date");
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                        Date date = sdf.parse(dateString);

                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yy");
                        String formattedDate = outputFormat.format(date);
                        dateString = formattedDate;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    instructorArrayList.add(text + "   " + " - " + dateString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            instructorAdapter.notifyDataSetChanged();
            u.setListViewHeightBasedOnChildren(instructorList);
            dialog.dismiss();
        }
    }


       class HttpCommentPostTask extends AsyncTask<String, Void, String> {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute(){
                dialog = new ProgressDialog(context);
                dialog.setTitle("Submitting data");
                dialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... urls) {
                HttpPost postMethod = new HttpPost(urls[0]);
                StringEntity comment = null;
                try {
                    comment = new StringEntity(comments, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    Log.i("rew", e.toString());
                }
                postMethod.setHeader("Content-Type", "application/json;charset=UTF-8");
                postMethod.setEntity(comment);
                try {
                    HttpResponse responseBody = httpclient.execute(postMethod);
                } catch (Throwable t) {
                    Log.i("rew", t.toString());
                }
                return null;
            }

            public void onPostExecute(String jsonString) {
                dialog.dismiss();
                editcomment.setText("");
               // Toast.makeText(DetailActivity.this,"Success, Your comment is posted", Toast.LENGTH_SHORT).show();
                instructorAdapter.clear();

                HttpCommentTask commentTask = new HttpCommentTask();
                String commentUrl = "http://bismarck.sdsu.edu/rateme/comments/"+id;
                commentTask.execute(commentUrl);

            }
        }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("rew","key Down pressed");
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
            return true;
        }
      return false;
    }

}
