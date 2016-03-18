package com.smithahm.ratetheinstructor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

   // HttpClient httpclient;
    Boolean fromDetailActivity = false;
    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;
    AndroidHttpClient httpclient;
    ListView instructorList;
    ArrayList<String> instructorArrayList = new ArrayList<String>();
    ArrayAdapter<String> instructorAdapter;
    Context context;
    private Long id;
    public final static String EXTRA_MESSAGE = "com.smithahm.ratetheinstructor.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getSupportActionBar();
            actionBar.setIcon(R.drawable.ic_launcher);
            actionBar.setLogo(R.drawable.ic_launcher);
        }

        instructorList= (ListView)findViewById(R.id.instList);
        instructorAdapter = new ArrayAdapter<String>(this, R.layout.instructor_items, instructorArrayList);
        instructorList.setAdapter(instructorAdapter);
        instructorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               view.setBackgroundColor(Color.GRAY);
               Intent intent = new Intent(MainActivity.this, DetailActivity.class);
               intent.putExtra(EXTRA_MESSAGE,id);
               startActivityForResult(intent, 1);
            }
        });
    }

    public void onResume() {
        super.onResume();
        Log.i("rew","onResume called");
        String userAgent = null;
        httpclient = AndroidHttpClient.newInstance(userAgent);

        //On Key down clear the list
        if(fromDetailActivity) {
           instructorAdapter.clear();
        }
            HttpClientTask task = new HttpClientTask();
            String url = "http://bismarck.sdsu.edu/rateme/list";
            task.execute(url);

    }

    public void onPause() {
        super.onPause();
      // httpclient.getConnectionManager().shutdown();
        httpclient.close();
    }

    class HttpClientTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
           dialog = new ProgressDialog(context);
           dialog.setTitle("Loading data");
           dialog.show();
           super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String responseBody = null;
            try {
                ResponseHandler<String> responseHandler =
                        new BasicResponseHandler();
                HttpGet getMethod = new HttpGet(urls[0]);
                responseBody = httpclient.execute(getMethod,
                        responseHandler);
                return responseBody;
            } catch (Throwable t) {
                Log.i("rew", "did not work", t);
            }

            return responseBody;
        }

        @Override
        public void onPostExecute(String jsonString) {
            try {
                JSONArray data = new JSONArray(jsonString);
                for(int i=0; i<data.length(); i++) {
                    JSONObject firstPerson = (JSONObject) data.get(i);
                    String firstName = firstPerson.getString("firstName");
                    String lastName = firstPerson.getString("lastName");
                    id=firstPerson.getLong("id");
                    instructorArrayList.add(id + ".  " + firstName + " " + lastName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            instructorAdapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    fromDetailActivity = true;
                    break;
                default:
                    break;
            }
        }

    }


}
