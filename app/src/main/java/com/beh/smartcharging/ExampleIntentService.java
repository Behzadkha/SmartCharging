package com.beh.smartcharging;

import android.app.IntentService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static com.beh.smartcharging.App.CHANNEL_ID;
import static com.beh.smartcharging.MainActivity.WEBHOOKPASS;
import static com.beh.smartcharging.MainActivity.deleteservice;

public class ExampleIntentService extends IntentService {
    private static final String TAG = "ExampleIntentService";

    private PowerManager.WakeLock wakeLock;
    public static int level = 5;

    public ExampleIntentService() {
        super("ExampleIntentService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getwebhook();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExampleApp:Wakelock");
        wakeLock.acquire();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Example IntentService")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String input = intent.getStringExtra("batterylevel");
        int desiredpercentage = Integer.parseInt(input);
        System.out.println("isitchanged "+input);
        Uri uri = Uri.parse("https://maker.ifttt.com/trigger/battery_charged/with/key/"+WEBHOOKPASS);
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int percentage = 0;
        percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        while(percentage != desiredpercentage){
            System.out.println("checkingbattery " + percentage + " desired percentage " + desiredpercentage);
            SystemClock.sleep(60000);
            percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        if(percentage == desiredpercentage){
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://maker.ifttt.com/trigger/battery_charged/with/key/"+WEBHOOKPASS);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            System.exit(0);
        }
        /*Intent intente = new Intent(Intent.ACTION_VIEW, uri);
        intente.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intente);*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }
    private void getwebhook(){
        try{
            Scanner in = new Scanner(new File("webhookpass"));
            String temp = "";
            while(in.hasNextLine()){
                temp = in.nextLine();
            }
            System.out.println("getting the webhook " + temp);
        }catch (Exception e){
            System.out.println(e);
        }

    }
}
