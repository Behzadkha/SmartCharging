package com.beh.smartcharging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private TextView batteryTxt;
    private EditText desirelevel;
    private Button btnchange;
    private EditText webhook;
    public static String WEBHOOKPASS;//gets it from the user and is used in setservice()
    private static int level;
    public static boolean deleteservice = false;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        batteryTxt = (TextView) this.findViewById(R.id.batterypercentage);
        desirelevel = (EditText) this.findViewById(R.id.chargetostop);
        btnchange = (Button) this.findViewById(R.id.btn_change);
        webhook = (EditText) this.findViewById(R.id.webhook);
        readfile();
        final Intent serviceIntent = new Intent(this, ExampleIntentService.class);
        stopService(serviceIntent);
        //this.registerReceiver(this.mb, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        listeners();
    }
    public void startService(String batterylevel){
        Intent serviceIntent = new Intent(this, ExampleIntentService.class);
        serviceIntent.putExtra("batterylevel", batterylevel);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void listeners(){
        final Intent serviceIntent = new Intent(this, ExampleIntentService.class);
        btnchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!webhook.getText().toString().equals("") && !desirelevel.getText().toString().equals("")){
                    if(Integer.parseInt(desirelevel.getText().toString()) > 0 &&  Integer.parseInt(desirelevel.getText().toString()) <= 100){
                        setwebhook();
                        stopService(serviceIntent);
                        String battery = desirelevel.getText().toString();
                        startService(battery);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Desired percentage has to be between 0 and 100", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Can't leave any input empty", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    public void setwebhook(){

        try {
            FileOutputStream fileout=openFileOutput("webhook.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(webhook.getText().toString());
            outputWriter.close();
            //using webhookpass in the exampleintentservice as the url
            WEBHOOKPASS = webhook.getText().toString();
            //display file saved message
            Toast.makeText(getBaseContext(), "SERVICE STARTED",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readfile(){
        File filee = getFileStreamPath("webhook.txt");
        if(filee.exists()){
            try {
                FileInputStream fis = context.openFileInput("webhook.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                webhook.setText(sb);
            } catch (IOException e) {
                System.out.println("IMINERROR" + e);
                e.printStackTrace();
            }
        }
        else{
            webhook.setText("WEBHOOK");
        }

    }


}