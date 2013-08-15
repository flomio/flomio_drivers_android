/** Copyright (C) 2009 by Aleksey Surkov.
 **
 **  Modified by Richard Grundy on 8/6/13.
 **  Flomio, Inc.
 */

package com.flomio.flojackexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private String LOG_TAG = "MainActivity";
    public static final String SM_BCAST_SCAN = "com.restock.serialmagic.gears.action.SCAN";
    private Handler mHandler;
    private BroadcastReceiver mScanReceiver;
    private IntentFilter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        Intent i = new Intent(MainActivity.this, FJNFCService.class);
        try{
            PendingIntent.getService(MainActivity.this, 0, i, 0).send();
        }catch(Exception e){
            Log.e(LOG_TAG, "failed with " + e);
        }

        IntentFilter mFilter = new IntentFilter(SM_BCAST_SCAN);
        BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getAction();
                if (s.equals(SM_BCAST_SCAN)) {
                    String scan = intent.getStringExtra("scan");
                    Log.d(LOG_TAG, "SCAN!!" + scan);
                    MainActivity.this.PostToUI(scan);
                }
            }
        };
        registerReceiver(mScanReceiver, mFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Toggle " + item.getTitle() + "?");
        switch (item.getItemId()) {
            case R.id.action_14443A:
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //FloJack Toggle Code: 14443A
                    }
                });
                // Create the AlertDialog object and return it
                dialog.create().show();
                return true;
            case R.id.action_15693:
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //FloJack Toggle Code: 15693
                    }
                });
                // Create the AlertDialog object and return it
                dialog.create().show();
                return true;
            case R.id.action_Felica:
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //FloJack Toggle Code: Felica
                    }
                });
                // Create the AlertDialog object and return it
                dialog.create().show();
                return true;
            case R.id.action_info:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://flomio.com/flojack"));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void PostToUI(final String msg) {
        mHandler.post(new Runnable() {
            public void run() {
                TextView temp = (TextView) MainActivity.this.findViewById(R.id.outputTextView);
                temp.append("\n" + msg);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        setKeepScreenOn(this, true);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mScanReceiver);
        setKeepScreenOn(this, false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void setKeepScreenOn(Activity activity, boolean keepScreenOn) {
        if (keepScreenOn) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
