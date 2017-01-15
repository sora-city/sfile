package com.example.kuntan.sfile;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InputPassword extends AppCompatActivity {
    private Button btnOkay = null;
    private EditText edtPasscode = null;
    private String passcode = "";

    public void CloseActivity (int RetCode ) {
        Bundle bundle = new Bundle ();
        bundle.putString ("passcode", passcode);

        Intent it = new Intent();
        it.setClass (InputPassword.this, MainActivity.class);
        it.putExtras (bundle);

        setResult(RetCode, it);
        finish ();
    }

    public void OnClickOkay (View v) {
        edtPasscode = (EditText) findViewById(R.id.psfText);

        passcode = edtPasscode.getText().toString();
        CloseActivity(Activity.RESULT_OK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password);

        btnOkay = (Button) findViewById(R.id.btn_okay);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickOkay(v);

            }
        });
    }
}
