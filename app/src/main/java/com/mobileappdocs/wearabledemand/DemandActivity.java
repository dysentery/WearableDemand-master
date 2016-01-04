package com.mobileappdocs.wearabledemand;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

/****************/
/************/

public class DemandActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    public static final String ACTION_DEMAND = "com.androidweardocs.ACTION_DEMAND";
    public static final String EXTRA_MESSAGE = "com.androidweardocs.EXTRA_MESSAGE";
    public static final String EXTRA_VOICE_REPLY = "com.androidweardocs.EXTRA_VOICE_REPLY";

    // Log
    private static final String TAG = "demand_activity";

    // Speach
    private static TextToSpeech engine;
    private static String text = "";
    private static String demand = "";
    public static final String URL = "http://quandyfactory.com/insult/json";
    public static String insultData;
    /************************************************************/
    private static Toolbar toolbar;
    private static FloatingActionButton fab;
    private static Intent demandIntent;
    private static PendingIntent demandPendingIntent;
    private static String replyLabel;
    private static RemoteInput remoteInput;
    private static NotificationCompat.Action replyAction;
    private static NotificationCompat.WearableExtender wearableExtender;
    private static Notification notification;
    private static NotificationManagerCompat notificationManager;
    private static int notificationId;
    private static IntentFilter messageFilter;
    private static MessageReceiver messageReceiver;
    /****************************/
    BitmapDrawable flip(BitmapDrawable d, Context context)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap src = d.getBitmap();
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return new BitmapDrawable(context.getResources(), dst);
    }
    /*********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demand);

        // Speach
        engine = new TextToSpeech(this, this);

        //Log.d(TAG, "*------------------- onCreate ------------------------ *");
        // Get initial insult from url
        new SimpleTask().execute(URL);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Create an Intent for the demand
        demandIntent = new Intent(this, DemandIntentReceiver.class)
                .putExtra(EXTRA_MESSAGE, "Reply icon selected.")
                .setAction(ACTION_DEMAND);

        // Create a pending intent using the local broadcast receiver
        demandPendingIntent =
                PendingIntent.getBroadcast(this, 0, demandIntent, 0);

        // Create RemoteInput object for a voice reply (demand)
        replyLabel = getResources().getString(R.string.app_name);
        remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();

        // Create a wearable action
        replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                        getString(R.string.reply_label), demandPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Bitmap bitmap;
        bitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.cheeky_monkey_hi_hi_green);//shakespearean);
        //bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*1.1), (int)(bitmap.getHeight()*1.1), true);
        Bitmap rescaledBitmap = Bitmap.createScaledBitmap(bitmap, (400), (370), true);
        BitmapDrawable bitmapdrawable = new BitmapDrawable(getResources(), rescaledBitmap);
        BitmapDrawable flippedBitmap = flip(bitmapdrawable, getApplicationContext());
        // Create a wearable extender for a notification
        wearableExtender =
                new NotificationCompat.WearableExtender()
                        .addAction(replyAction)
                        //.setBackground(BitmapFactory.decodeResource(getResources(),
                        //        R.drawable.cheeky_monkey_th));//.build(
                        .setBackground(flippedBitmap.getBitmap());
                        //.setContentIcon(R.drawable.cheeky_monkey_th);

        // Create a notification and extend it for the wearable
        notification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Hello!")
                        .setContentText("Swipe left, hit reply and say your name.")
                        .setSmallIcon(R.drawable.ic_launcher)//.wear_notofication)
                        .extend(wearableExtender)
                        .build();

        // Get the notification manager
        notificationManager =
                NotificationManagerCompat.from(this);

        // Dispatch the extended notification
        notificationId = 1;
        notificationManager.notify(notificationId, notification);

        // Register the local broadcast receiver for the users demand.
        messageFilter = new IntentFilter(Intent.ACTION_SEND);
        messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).
                registerReceiver(messageReceiver, messageFilter);

        if (getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().
        heightPixels) {
            Toast.makeText(this, "Screen switched to Landscape mode", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Screen switched to Portrait mode", Toast.LENGTH_SHORT).show();
        }

        TextView demandView = (TextView) findViewById(R.id.demand_text);
        demandView.setText( text );
    }
    // Speech
    public void speakText(String speechText) {
        // speak() would work on if you have set minSDK version 21 or higher
        engine.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            //Setting speech Language
            engine.setLanguage(Locale.ENGLISH);
            engine.setPitch(1);
        }
    }
    // URL call
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder buffer = new StringBuilder();
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

            TextView demandView = (TextView) findViewById(R.id.demand_text);
            demandView.setText( text );
            speakText(text);
        }
        @Override
        protected String doInBackground(String... urls) {
            InsultData msg;
            String Result = "";
            try {
                msg = new Gson().fromJson(readUrl("http://quandyfactory.com/insult/json"), InsultData.class);
                Result = insultData = msg.getInsult();
                Log.d (TAG, "readURL");
            } catch (Exception E) {
                Log.d(TAG, E.toString());
            }
            return Result;
        }

        @Override
        protected void onPostExecute(String rtnInsult) {

        }
    }
    /************************************************************/

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
            // Trigger next insult url read and only then trigger speech and text display
            String demand = intent.getStringExtra("reply");

            //insultData = " " + insultData;
            text = getString(R.string.demand, demand ) + " " + insultData;
            text = text.toUpperCase().charAt(0) + text.substring(1);

            // Get next insult
            new SimpleTask().execute(URL);
        }
    }
}
