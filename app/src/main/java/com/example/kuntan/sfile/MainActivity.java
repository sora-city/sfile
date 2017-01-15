package com.example.kuntan.sfile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

public class MainActivity extends AppCompatActivity {
    static String tag = "sfile";
    static String sfname = "sfile.bin";
    static String ftext = "";
    static int set_password_code = 1001;
    static int input_password_code = 1002;
    static String passcode = "";

    public void toastMsg ( String text ) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public static boolean isFileExist ( String fname ) {
        File f = new File (fname);
        if ( f.exists() && !f.isDirectory())
            return true;
        else
            return false;
    }

    public void LoadSecureText () {
        Intent intent = new Intent(getApplicationContext(), InputPassword.class);
        startActivityForResult(intent, input_password_code);
    }

    public void LoadSecureText2 () {
        SecureFile sf = new SecureFile();

        SecureFile.SF_ERROR err = sf.LoadSecureFile(sfname, passcode);
        if ( err == SecureFile.SF_ERROR.SUCCESS) {
            EditText v = (EditText) findViewById(R.id.editText);
            v.setText(sf.getText());
        } else {
            Log.d (tag, "Error code " + err.toString());
            finish ();
        }

    }
    public void SaveCurrentText () {
        // display setpassword activity
        Intent intent = new Intent(getApplicationContext(), SetPassword.class);
        startActivityForResult(intent, set_password_code);
    }

    public void SaveCurrentText2 () {
        // passcode should be set
        SecureFile sf = new SecureFile();
        sf.setText(ftext);
        sf.SaveSecureFile(sfname, passcode);

    }
    static public boolean hasStoragePermissions (Activity app) {
        int permission = ActivityCompat.checkSelfPermission(app, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (permission == PackageManager.PERMISSION_GRANTED);
    }

    static public boolean GetStoragePermissions (Activity app) {
        int requestID = 1;
        String[] PERMISSION_STORAGE = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!hasStoragePermissions(app)) {
            ActivityCompat.requestPermissions(app, PERMISSION_STORAGE, requestID);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GetStoragePermissions(this);
        try {
            sfname = Environment.getExternalStorageDirectory().getCanonicalPath() + "/sfile.bin";
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        EditText v = (EditText) findViewById(R.id.editText);
        try {
            v.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(),
                    "fonts/DroidSansChinese.ttf"));
        }  catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(tag, sfname);
        if ( !isFileExist(sfname)) {
            ftext = "hello";

            v.setText(ftext);
            SaveCurrentText ();
        } else {
            LoadSecureText();
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data ) {
        if (requestCode == set_password_code ) {
            if (resultCode == Activity.RESULT_OK ) {
                Bundle b = data.getExtras();
                passcode = b.getString("passcode");
                SaveCurrentText2 ();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(tag, "Set passcode canceled!\n");
                finish();
            }
        } else if (requestCode == input_password_code ) {
            if (resultCode == Activity.RESULT_OK ) {
                Bundle b = data.getExtras();
                passcode = b.getString("passcode");
                Log.d (tag, passcode);
                LoadSecureText2();
            } else {
                Log.d(tag, "Input passcode canceled!\n");
                finish();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            EditText v = (EditText) findViewById(R.id.editText);
            ftext = v.getText().toString();
            SaveCurrentText();
            return true;
        } else if (id == R.id.action_exit) {
            finish ();
            return true;
        }

            return super.onOptionsItemSelected(item);
    }
}
