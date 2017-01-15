package com.example.kuntan.sfile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetPassword extends AppCompatActivity {
    private Button btnOkay = null;
    private Button btnCancel = null;
    private EditText edtPasscode = null;
    private EditText edtRePasscode = null;

    private String passcode = "";

    public void CloseActivity (int RetCode ) {
        Bundle bundle = new Bundle ();
        bundle.putString ("passcode", passcode);

        Intent it = new Intent();
        it.setClass (SetPassword.this, MainActivity.class);
        it.putExtras (bundle);

        setResult(RetCode, it);
        finish ();
    }

    @Override
    public void onBackPressed () {
        CloseActivity(Activity.RESULT_CANCELED);
    }

    public void OnClickOkay (View v) {
        edtPasscode = (EditText) findViewById(R.id.psfText);
        if (edtPasscode.getText().equals("") ) {
            Toast.makeText(this, "Please enter a passcode!", Toast.LENGTH_SHORT).show();
            return;
        }

        edtRePasscode = (EditText) findViewById(R.id.psfText2);
        String p1 = edtPasscode.getText().toString();
        String p2 = edtRePasscode.getText ().toString();

        if ( !p1.equals(p2) ) {
            Toast.makeText(this, "Please enter a passcode!", Toast.LENGTH_SHORT).show();
            return;
        }
        passcode = p1;
        CloseActivity(Activity.RESULT_OK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnOkay = (Button) findViewById(R.id.btn_okay);
        btnOkay.setOnClickListener( new View.OnClickListener() {
            @Override
        public void onClick (View v) {
                OnClickOkay(v);

            }
        });

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener( new View.OnClickListener() {
            @Override
        public void onClick (View v) {
                CloseActivity ( Activity.RESULT_CANCELED);
            }
        });

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
