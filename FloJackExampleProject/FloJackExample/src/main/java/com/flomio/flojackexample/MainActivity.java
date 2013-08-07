package com.flomio.flojackexample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FJNFCService fjnfcService = new FJNFCService();
        fjnfcService.initialize();
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
            case R.id.action_felica:
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //FloJack Toggle Code: Felica
                    }
                });
                // Create the AlertDialog object and return it
                dialog.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
