package com.mobileappdocs.wearabledemand;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/****************/
/************/

public class DemandActivity extends AppCompatActivity {

    public static final String ACTION_DEMAND = "com.androidweardocs.ACTION_DEMAND";
    public static final String EXTRA_MESSAGE = "com.androidweardocs.EXTRA_MESSAGE";
    public static final String EXTRA_VOICE_REPLY = "com.androidweardocs.EXTRA_VOICE_REPLY";
    private static final String TAG = "MyActivity";

    /************************************************************/
    public static final String URL = "http://quandyfactory.com/insult/json";
    public static String data;
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        Log.d (TAG, "readURL");
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    public class SimpleTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // Create Show ProgressBar
            //Log.d(TAG, "good");
        }

        @Override
        protected String doInBackground(String... urls) {
            //android.os.Debug.waitForDebugger();
            //
            //String data;
            InsultData msg;
            String Result = "";
            try {
                data = readUrl("http://quandyfactory.com/insult/json");
                msg = new Gson().fromJson(data, InsultData.class);
                data = msg.getInsult();
            } catch (Exception E) {
                Log.d(TAG, E.toString());
            }
            return Result;
        }

        @Override
        protected void onPostExecute(String JsonString) {
            Log.d (TAG, "onPostExecute");
        }
    }
        /************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        new SimpleTask().execute(URL);

        setSupportActionBar(toolbar);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Create an Intent for the demand
        Intent demandIntent = new Intent(this, DemandIntentReceiver.class)
                .putExtra(EXTRA_MESSAGE, "Reply icon selected.")
                .setAction(ACTION_DEMAND);

        // Create a pending intent using the local broadcast receiver
        PendingIntent demandPendingIntent =
                PendingIntent.getBroadcast(this, 0, demandIntent, 0);

        // Create RemoteInput object for a voice reply (demand)
        String replyLabel = getResources().getString(R.string.app_name);
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();

        // Create a wearable action
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                        getString(R.string.reply_label), demandPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        // Create a wearable extender for a notification
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .addAction(replyAction);

        // Create a notification and extend it for the wearable
        Notification notification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Hello!")
                        .setContentText("Swipe left, hit reply and say your name.")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .extend(wearableExtender)
                        .build();
        // Get the notification manager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Dispatch the extended notification
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);


        // Register the local broadcast receiver for the users demand.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).

        registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demand, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Class to receive demand text from the wearable demand receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Display the received demand
            TextView demandView = (TextView) findViewById(R.id.demand_text);
            String demand = demandView.getText() + intent.getStringExtra("reply");

            //String data = "Null";
            new SimpleTask().execute(URL);

            Log.d (TAG, "this is an info message");

            demandView.setText(demand + " " + data);// msg.getInsult() );//msg.getInsult()
           // glblName = demand;
        }
    }

}
