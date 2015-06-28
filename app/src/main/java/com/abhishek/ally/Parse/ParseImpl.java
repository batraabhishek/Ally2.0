package com.abhishek.ally.Parse;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/*
 * ParseImpl.java
 * Handles the Parse implementation. You will need to sign up for a Parse account and set the
 *  Application ID and Client Key in order to sign into Parse and manage your users.
 *
 *  This class handles the "Friends List" (all users in your Parse account), as well as some general
 *   helper functions.
 */

public class ParseImpl {

    //If you haven't, make sure you set up a Parse account at http://parse.com, then follow these
    // instructions:
    // 1. Log into your Parse account
    // 2. Mouse over the settings option in your project (gear icon)
    // 3. Select "Keys"
    // 4. Copy the "Application ID" and "Client Key" into the following fields:
    private static String ParseAppID = "0MMopA5bR1toNEFLW02ut9R5RXadxwY5ptWM90Yd";
    private static String ParseClientKey = "krZ5xTqVDUfG5kVMypR5N2O8PGZwyWpf6HbQ9Bqj";
    //We keep track of all users associated with this app in Parse. You can override this to implement
    // your own user management system (based on a friends list, for example)
    private static HashMap<String, ParseUser> allUsers;

    //Merely checks to see if you have updated the App ID and Client Key. If these are set up
    // incorrectly, Parse will fail to initialize
    public static boolean hasValidAppID() {
        if (ParseAppID.equals("PARSE_APP_ID") || ParseClientKey.equals("PARSE_CLIENT_KEY")) {
            return false;
        }

        return true;
    }

    //Called from the Application class to set up Parse
    public static void initialize(Context context) {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(context.getApplicationContext());
        Parse.initialize(context.getApplicationContext(), ParseAppID, ParseClientKey);
    }

    //When the user tries to log in, pass in the Username and Password they supplied, then execute
    // the appropriate callback on success/failure
    public static void loginUser(String username, String password, final ParseLoginCallbacks callback) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // The user is logged in.
                    if (callback != null)
                        callback.loginSucceeded(user);
                    Log.d("Parse", "Parse user login success: " + user.getUsername());
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    if (callback != null)
                        callback.loginFailed(e);
                    Log.d("Parse", "Parse user login failed");
                    Log.d("Parse", e.toString());
                }
            }
        });
    }

    //Allow a new user to register. Pass in their details. On success, they will be logged in, and
    // we can continue the sign in flow
    public static void registerUser(String username, String password, String email, String name, final ParseLoginCallbacks callback) {

        final ParseUser user = new ParseUser();
        user.put("name", name);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    callback.loginSucceeded(user);
                } else {
                    callback.loginFailed(e);
                }
            }
        });

    }

    //Returns the ParseUser object of the currently signed in user
    public static ParseUser getRegisteredUser() {
        return ParseUser.getCurrentUser();
    }

    public static void cacheAllUsers() {

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    allUsers = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        allUsers.put(results.get(i).getObjectId(), results.get(i));
                    }
                }
            }
        });
    }

    //Returns all users NOT including the currently signed in user
    public static Set<String> getAllFriends() {
        Set<String> friends = allUsers.keySet();
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        if (friends.contains(currentUserId))
            friends.remove(currentUserId);
        return friends;
    }

    //Takes a ParseObject id and returns the associated username (handle) for display purposes
    public static String getUsername(String id) {

        //Does this id appear in the "all users" list?
        if (id != null && allUsers != null && allUsers.containsKey(id) && allUsers.get(id) != null)
            return allUsers.get(id).getUsername();

        //Does this id belong to the currently signed in user?
        if (id != null && ParseUser.getCurrentUser() != null && id.equals(ParseUser.getCurrentUser().getObjectId()))
            return ParseUser.getCurrentUser().getUsername();

        //If the handle can't be found, return whatever value was passed in
        return id;
    }

    public static String getName(String id) {
        if (id != null && allUsers != null && allUsers.containsKey(id) && allUsers.get(id) != null)
            return (String)allUsers.get(id).get("name");

        //Does this id belong to the currently signed in user?
        if (id != null && ParseUser.getCurrentUser() != null && id.equals(ParseUser.getCurrentUser().getObjectId()))
            return (String)ParseUser.getCurrentUser().get("name");

        //If the handle can't be found, return whatever value was passed in
        return id;
    }

    //Allow the user to reset their password based on their email address
    public static void resetPassword(String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ParseUser.requestPasswordResetInBackground(email);
        }
    }
}
