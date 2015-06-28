package com.abhishek.ally.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;

import com.abhishek.ally.Layer.LayerCallbacks;
import com.abhishek.ally.Layer.LayerImpl;
import com.abhishek.ally.Parse.ParseImpl;
import com.layer.sdk.exceptions.LayerException;

/*
 * ActivityBase.java
 * The base class that takes care of handling Layer, Parse and related callbacks. Every Activity
 *  extends this class in order to overload the callbacks. Also has code to detect when a view has
 *  been resized due to opening or closing the keyboard.
 */

public class ActivityBase extends AppCompatActivity implements LayerCallbacks, View.OnClickListener {

    //Detect when the keyboard is showing or not
    //Used to adjust the view in the MessageActivity when the Keyboard changes the view size
    private boolean mKeyboardListenersAttached = false;
    private ViewGroup targetView;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = targetView.getRootView().getHeight() - targetView.getHeight();
            int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

            if (heightDiff <= contentViewTop) {
                onHideKeyboard();
            } else {
                int keyboardHeight = heightDiff - contentViewTop;
                onShowKeyboard(keyboardHeight);
            }
        }
    };

    //Makes sure the Layer Client is created, registers callback handlers, and connects
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializes and connects the LayerClient if it hasn't been created already
        LayerImpl.initialize(getApplicationContext());

        //Registers the activity so callbacks are executed on the correct class
        LayerImpl.setContext(this);
    }

    //This can be called when the app moves from the foreground to the background, and when the
    // Activity is created
    protected void onResume() {
        super.onResume();

        //Registers the activity so callbacks are executed on the correct class
        LayerImpl.setContext(this);

        //Runs a Parse Query to return all users registered with the app
        ParseImpl.cacheAllUsers();
    }

    //Handler to put up an alert dialog
    protected void showAlert(String heading, String body) {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(body)
                .setTitle(heading);

        // Get the AlertDialog from create() and then show() it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Handler to return the text contained in an EditText object
    protected String getTextAsString(EditText view) {

        if (view != null && view.getText() != null)
            return view.getText().toString();

        return "";
    }

    //Layer connection callbacks
    public void onLayerConnected() {
    }

    public void onLayerDisconnected() {
    }

    public void onLayerConnectionError(LayerException e) {
    }

    //Layer authentication callbacks
    public void onUserAuthenticated(String id) {
    }

    public void onUserAuthenticatedError(LayerException e) {
    }

    public void onUserDeauthenticated() {
    }

    //OnClickListener callback
    public void onClick(View v) {
    }

    //Keyboard showing/hidden callbacks
    protected void onShowKeyboard(int keyboardHeight) {
    }

    protected void onHideKeyboard() {
    }

    protected void attachKeyboardListeners(ViewGroup group) {
        if (mKeyboardListenersAttached) {
            return;
        }

        targetView = group;
        if (targetView != null) {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

            mKeyboardListenersAttached = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mKeyboardListenersAttached) {
            targetView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
    }
}
