package com.abhishek.ally.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.abhishek.ally.Adapters.MessageQueryAdapter;
import com.abhishek.ally.Adapters.QueryAdapter;
import com.abhishek.ally.Layer.LayerImpl;
import com.abhishek.ally.Parse.ParseImpl;
import com.abhhishek.ally.R;
import com.abhishek.ally.VolleySingleton;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MessageActivity extends ActivityBase implements MessageQueryAdapter.MessageClickHandler {


//    -9 : ~@~
//            -8 : :{
//        -7 : >.<
//        -6 : :=(
//                -5 : x'(
//                -4 : x-(
//                -3 : :-O
//                -2 : :-(
//                -1 : :-/
//                0 : :-|
//                1 : :-)
//        2 : :-)
//        3 : =)
//        4 : ;-)
//        5 : B-)
//        6 : =D
//        7 : :-D
//        8 : :))
//        9 : <3


    private static final String[] emoticons = {":x", ":x", ":{", ">.<",
            ">.<", "=(", "=(", ":-(", ":-(", ":-/",
            ":-|", ":-)", ":-)", ":-)", ";-)", "B-)", "B-)", ":-D", ":-D", "<3", "<3"
    };


    private Conversation mConversation;
    private MessageQueryAdapter mMessagesAdapter;
    private RecyclerView mMessagesView;
    private ArrayList<String> mTargetParticipants;
    private Menu mMenu;
    private boolean hideMenu;
    private TextView tv;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_screen);
        hideMenu = false;
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mMessagesView = (RecyclerView) findViewById(R.id.mRecyclerView);
        Button sendButton = (Button) findViewById(R.id.sendButton);
        if (sendButton != null)
            sendButton.setOnClickListener(this);
        attachKeyboardListeners(mMessagesView);
    }

    public void onResume() {
        super.onResume();
        if (!LayerImpl.isAuthenticated()) {

            if (ParseImpl.getRegisteredUser() == null) {

                Intent intent = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(intent);

            } else {
                LayerImpl.authenticateUser();

            }

        } else {

            Uri conversationURI = getIntent().getParcelableExtra("conversation-id");
            if (conversationURI != null)
                mConversation = LayerImpl.getLayerClient().getConversation(conversationURI);

            if (mConversation != null)
                setupMessagesView();
            else
                createNewConversationView();
        }
    }

    private void setupMessagesView() {


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);
        createMessagesAdapter();
        populateToField(mConversation.getParticipants());
        hideAddParticipantsButton();
    }


    private void populateToField(List<String> participantIds) {

        String participants = "";
        int idx = 0;
        for (String id : participantIds) {
            if (!id.equals(LayerImpl.getLayerClient().getAuthenticatedUserId())) {

                if (idx != 0) {
                    participants += participants + ", ";
                }

                String user = ParseImpl.getName(id);
                String[] userDiv = user.split(" ");
                participants += userDiv[0];

                idx++;
            }
        }

        getSupportActionBar().setTitle(participants);

    }

    private void createNewConversationView() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);
    }

    private void createMessagesAdapter() {


        mMessagesAdapter = new MessageQueryAdapter(getApplicationContext(), LayerImpl.getLayerClient(), mMessagesView, mConversation, this, new QueryAdapter.Callback() {


            String conCatText = "";


            public void onItemInserted() {
                mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
                if (conCatText.length() > 1000) {
                    conCatText = "";
                }

                conCatText += LayerImpl.getMessageText(mConversation.getLastMessage()) + ". ";

                sendRequest(MessageActivity.this, conCatText);

            }


        });
        mMessagesView.setAdapter(mMessagesAdapter);
        mMessagesAdapter.refresh();
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    public void onMessageClick(Message message) {

    }

    public boolean onMessageLongClick(Message message) {
        return false;
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendButton:
                Log.d("Activity", "Send button pressed");
                sendMessage();
                break;

        }
    }

    private void sendMessage() {

        if (mConversation == null) {

            if (mTargetParticipants == null) {
                showAlert("Send Message Error", "You need to specify at least one participant before sending a message.");
                return;
            } else if (mTargetParticipants.size() > 1) {

                mConversation = LayerImpl.getLayerClient().newConversation(mTargetParticipants);
                createMessagesAdapter();
                hideAddParticipantsButton();

            } else {
                showAlert("Send Message Error", "You need to specify at least one participant before sending a message.");
                return;
            }
        }

        EditText input = (EditText) findViewById(R.id.textInput);
        String text = getTextAsString(input);

        if (mConversation != null && text != null && text.length() > 0) {

            MessagePart part = LayerImpl.getLayerClient().newMessagePart(text);
            Message msg = LayerImpl.getLayerClient().newMessage(part);
            mConversation.send(msg);

            input.setText("");

        } else {
            showAlert("Send Message Error", "You cannot send an empty message.");
        }
    }

    //Shows a list of all users that can be added to the Conversation
    private void showParticipantPicker() {

        //Update user list from Parse
        ParseImpl.cacheAllUsers();

        //Create a new Dialog Box
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Select Participants");
        helpBuilder.setMessage("Add or remove participants from this conversation:\n");

        //The Linear Layout View that will hold all the CheckBox views
        LinearLayout checkboxList = new LinearLayout(this);
        checkboxList.setOrientation(LinearLayout.VERTICAL);

        //Grab a list of all friends
        Set friends = ParseImpl.getAllFriends();

        //A Map of the CheckBox with the human readable username and the Parse Object ID
        final HashMap<CheckBox, String> allUsers = new HashMap<>();

        //Create the list of participants if it hasn't been instantiated
        if (mTargetParticipants == null)
            mTargetParticipants = new ArrayList<>();

        //Go through each friend and create a Checkbox with a human readable name mapped to the
        // Object ID
        Iterator itr = friends.iterator();
        while (itr.hasNext()) {
            String friendId = (String) itr.next();

            CheckBox friend = new CheckBox(this);
            friend.setText(ParseImpl.getName(friendId) + ": @" + ParseImpl.getUsername(friendId));

            //If this user is already selected, mark the checkbox
            if (mTargetParticipants.contains(friendId))
                friend.setChecked(true);

            checkboxList.addView(friend);

            allUsers.put(friend, friendId);
        }

        //Add the list of CheckBoxes to the Alert Dialog
        helpBuilder.setView(checkboxList);

        //When the user is done adding/removing participants, update the list of target users
        helpBuilder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                        //Reset the target user list, and rebuild it based on which checkboxes are selected
                        mTargetParticipants.clear();
                        mTargetParticipants.add(LayerImpl.getLayerClient().getAuthenticatedUserId());

                        Set checkboxes = allUsers.keySet();
                        Iterator checkItr = checkboxes.iterator();
                        while (checkItr.hasNext()) {
                            CheckBox currCheck = (CheckBox) checkItr.next();
                            if (currCheck != null && currCheck.isChecked()) {
                                String friendID = allUsers.get(currCheck);
                                mTargetParticipants.add(friendID);
                            }
                        }

                        Log.d("Activity", "Current participants: " + mTargetParticipants.toString());

                        //Draw the list of target users
                        populateToField(mTargetParticipants);
                    }
                });


        // Create and show the dialog box with list of all participants
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }

    //When a Conversation has Messages, we disable the ability to Add/Remove participants
    private void hideAddParticipantsButton() {
        try {
            mMenu.findItem(R.id.action_add_part).setVisible(false);
        } catch (NullPointerException e) {
            hideMenu = true;
        }
    }

    protected void onShowKeyboard(int keyboardHeight) {
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    protected void onHideKeyboard() {
        if (mMessagesView != null) {
            mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        tv = new TextView(this);
        tv.setText(":-|");
        tv.setTextColor(getResources().getColor(R.color.white_secondary_text));
        tv.setOnClickListener(this);
        tv.setPadding(5, 0, 5, 0);
        tv.setTextSize(16);
        tv.setPadding(20, 0, 20, 0);
        menu.add(0, 123, 1, "cols").setActionView(tv).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        this.mMenu = menu;
        if (hideMenu) {
            return true;
        }
        getMenuInflater().inflate(R.menu.menu_message_screen, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_part) {
            showParticipantPicker();
        }

        return true;
    }

    public void sendRequest(Context context, final String text) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        String url = "http://access.alchemyapi.com/calls/text/TextGetTextSentiment";

        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("apikey", "7579faa3bcd87e301a67e62298fbfeae52f1f899");
                params.put("outputMode", "json");
                params.put("text", text);
                return params;
            }
        };

        queue.add(request);
    }


    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
//            Log.d("JSON", response.toString());
            String perc;
            try {
                JSONObject jsonObject = new JSONObject(response);
                perc = jsonObject.getJSONObject("docSentiment").getString("score");
            } catch (JSONException e) {
                perc = "0";
                e.printStackTrace();
            }

            double percDouble = Double.parseDouble(perc);
            percDouble = percDouble * 10.0;

            int parInt = (int) percDouble;
            parInt += 10;

            Log.d("JSON", "Round Off : " + (int) percDouble);
            ((TextView) findViewById(R.id.emo_icon)).setText(emoticons[parInt]);

            if (percDouble == 0.0) {
                tv.setText("Neutral: " + emoticons[parInt]);
            } else if (percDouble < 0) {
                percDouble =  percDouble * (-10);
                int percentage = (int) percDouble;
                tv.setText("Negative: " + percentage +"%  " + emoticons[parInt]);
            } else {
                percDouble =  percDouble * (10);
                int percentage = (int) percDouble;
                tv.setText("Positive: " + percentage +"%  " + emoticons[parInt]);
            }
        }
    };

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("JSON", "Error: " + error.getMessage());
        }
    };

}