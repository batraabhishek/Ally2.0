package com.abhishek.ally.Application;

import android.app.Application;

import com.abhishek.ally.Parse.ParseImpl;

/*
 * MainApplication.java
 * Required to initialize Parse. Layer can be initialized in either the Application class or any
 *  Activity class. There are 3 things you need to take care of before you can run the app:
 *
 *  1. Create a Layer account and set your App ID in LayerImpl.java
 *  2. Create a Parse account and set your App ID and Client Key in ParseImpl.java
 *  3. Create a Parse function to Authenticate your user. See MyAuthenticationListener.java for more detail
 */

public class MainApplication extends Application {

    public void onCreate() {
        super.onCreate();
        ParseImpl.initialize(getApplicationContext());
    }
}
