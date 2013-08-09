/** Copyright (C) 2009 by Aleksey Surkov.
 **
 **  Modified by Richard Grundy on 8/6/13.
 **  Flomio, Inc.
 */

package com.flomio.flojackexample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    public FJNFCService mFJNFCService;
    public UIListener mUIListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create UIListener for FJNFCService to talk to MainActivity (via 'this' and 'new Handler')
        mUIListener= new UIListener(this, new Handler());
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

    public class UIListener implements FJNFCService.FJNFCListener {
        private MainActivity mParent;
        private Handler mHandler;

        public UIListener(Activity parent, Handler handler) {
            mParent = (MainActivity) parent;
            mHandler = handler;
        }

        private void PostToUI(final String msg) {
            mHandler.post(new Runnable() {
                public void run() {
                    TextView temp = (TextView) mParent.findViewById(R.id.outputTextView);
                    temp.append("\n" + msg);
                }
            });
        }

        public void onData(String parsedData) {
            PostToUI(parsedData);
        }

        public void onError(String error) {
            PostToUI(error);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Create FJNFCService that takes a Listener that implements OnData and OnError methods
        mFJNFCService = new FJNFCService(mUIListener);
        // Start the FJNFCService Thread. As separate thread it won't hang up MainActivity (UI Thread)
        mFJNFCService.start();

        setKeepScreenOn(this, true);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFJNFCService.interrupt();
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
