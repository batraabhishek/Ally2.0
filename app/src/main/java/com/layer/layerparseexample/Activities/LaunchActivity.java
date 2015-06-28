package com.layer.layerparseexample.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.layer.layerparseexample.Layer.LayerImpl;
import com.layer.layerparseexample.Parse.ParseImpl;
import com.layer.layerparseexample.R;

/*
 * LaunchActivity.java
 * Shows the initial loading page, and tries to initialize (and Connect) to Layer. Connecting early
 *  saves a few seconds when Authenticating since the initial handshake is already complete.
 */

public class LaunchActivity extends ActivityBase {

    //Make sure both the Layer and Parse configurations are set
    protected void onCreate(Bundle savedInstanceState) {
//        if (!LayerImpl.hasValidAppID()) {
//
//            showAlert("Invalid Layer App ID", "You will need a valid Layer App ID in order to run this example. " +
//                    "If you haven't already, create a Layer account at http://layer.com/signup and then follow these instructions:\n\n" +
//                    "1. Go to http://developer.layer.com and sign in\n" +
//                    "2. Select \"Info\" on the left panel\n" +
//                    "3. Copy the 'Staging App ID'\n" +
//                    "4. Paste that value in the LayerAppID String in LayerImpl.java");
//
//        } else if (!ParseImpl.hasValidAppID()) {
//
//            showAlert("Invalid Parse Credentials", "You will need a valid Parse project in order to run this example. " +
//                    "If you haven't already, create a Parse account at http://parse.com and follow these instructions:\n\n" +
//                    "1. Sign in and mouse over the Settings icon by your App" +
//                    "2. Select the \"Keys\" option" +
//                    "3. Copy the Application ID and Client Key" +
//                    "4. Paste those values in the ParseAppID and ParseClientKey fields in ParseImpl.java");
//
//        } else {
        //The base class will create a Layer object (since one should not exist) and connect
        super.onCreate(savedInstanceState);
        ParseImpl.cacheAllUsers();


        if (LayerImpl.isConnected()) {
            if (LayerImpl.isAuthenticated()) {
                Intent intent = new Intent(LaunchActivity.this, ConversationsActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(LaunchActivity.this, LoginSignUpActivity.class);
                startActivity(intent);
            }
            finish();
        }
        setContentView(R.layout.splash_screen);


    }


    @Override
    public void onLayerConnected() {


        if (LayerImpl.isAuthenticated()) {
            Intent intent = new Intent(LaunchActivity.this, ConversationsActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(LaunchActivity.this, LoginSignUpActivity.class);
            startActivity(intent);
        }
        finish();
    }


}
