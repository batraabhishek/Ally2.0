package com.layer.layerparseexample.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.layer.layerparseexample.R;

public class LoginSignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    public void login(View v) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void signup(View v) {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }
}
