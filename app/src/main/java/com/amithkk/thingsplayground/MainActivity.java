package com.amithkk.thingsplayground;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity {

    public static String AppTag = "amith";

    private class LedStripScanner extends AsyncTask<Void, Void, Void>
    {
        Apa102 ledstrip;
        public  LedStripScanner()
        {
            try {
                ledstrip = RainbowHat.openLedStrip();
            } catch (IOException e) {
                e.printStackTrace();
                    Log.d(AppTag, "LedStrip Could Not Be Opened");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(AppTag, "OnCancelled Called");
            try {
                int[] zerofill= new int[RainbowHat.LEDSTRIP_LENGTH];
                ledstrip.write(zerofill);
                ledstrip.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                ledstrip.setBrightness(1);
                int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
                for (int i = 0; i < rainbow.length; i++) {
                    rainbow[i] = Color.HSVToColor(255, new float[]{i * 360.f / rainbow.length, 1.0f, 1.0f});
                }
                int[] maskedrainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
                int i = 0, change = 1;
                while(true)
                {

                    Arrays.fill(maskedrainbow, 0);
                    if(isCancelled()) {
                        break;
                    }
                    maskedrainbow[i] = rainbow[i];
                    ledstrip.write(maskedrainbow);
                    Thread.sleep(50);
                    i += change;
                    if(i == maskedrainbow.length - 1)
                        change = -1;
                    else if(i == 0)
                        change = +1;


                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d(AppTag, "LedStrip Could Not Be Written To");
            }

            return null;

        }
    }


    private class ScrollerTask extends AsyncTask<String, Void, Void>
    {
        AlphanumericDisplay segment;
        int SEGMENT_COUNT = 4;

        public  ScrollerTask()
        {
            try {
                segment = RainbowHat.openDisplay();
                segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(AppTag, "Cannot Open SegDisplay");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            try {
                segment.clear();
                segment.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... messages) {
            try{

                String message = messages[0].toUpperCase() + "    ";
                while(true)
                {
                    segment.display(message);
                    int k = 1;
                    message =  message.substring(k) + message.substring(0, k);
                    Thread.sleep(200);
                    if(isCancelled())
                        break;
                }


            }
            catch (Exception e)
            {
                Log.d(AppTag, "SegDisplay Could Not Be Written To");
            }

            return null;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] currentString = {"Test String"};
        final Button scannerControlButton = findViewById(R.id.scanner_btn);
        final Button scrollerControlButton = findViewById(R.id.scroller_control);
        final Button scrollerUpdateButton = findViewById(R.id.scroller_update);
        final TextView scrollerText = findViewById(R.id.scroller_text);
        final ScrollerTask[] scroller = {new ScrollerTask()};
        scroller[0].execute(currentString[0]);
        final LedStripScanner[] scanner = {new LedStripScanner()};





        scannerControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!scanner[0].isCancelled()){
                    scanner[0].cancel(true);
                    scannerControlButton.setText("Start Scanner");
                }
                else{
                    scanner[0] = new LedStripScanner();
                    scanner[0].execute();
                    scannerControlButton.setText("Stop Scanner");
                }

            }
        });

        scrollerControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!scroller[0].isCancelled()){
                    scroller[0].cancel(true);
                    scrollerControlButton.setText("Start Scroller");
                }
                else{
                    scroller[0] = new ScrollerTask();
                    scroller[0].execute(currentString[0]);
                    scrollerControlButton.setText("Stop Scroller");
                }

            }
        });

        scrollerUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scrollerText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Empty Scroller Text", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!scroller[0].isCancelled())
                    scroller[0].cancel(true);
                currentString[0] =scrollerText.getText().toString();
                scroller[0] = new ScrollerTask();
                scroller[0].execute(currentString[0]);
            }
        });




    }

}
