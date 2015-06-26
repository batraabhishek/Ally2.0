package com.layer.layerparseexample.Activities;

import android.app.AlertDialog;
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

import com.layer.layerparseexample.Adapters.MessageQueryAdapter;
import com.layer.layerparseexample.Adapters.QueryAdapter;
import com.layer.layerparseexample.Layer.LayerImpl;
import com.layer.layerparseexample.Parse.ParseImpl;
import com.layer.layerparseexample.R;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MessageActivity extends ActivityBase implements MessageQueryAdapter.MessageClickHandler {

    private Conversation mConversation;
    private MessageQueryAdapter mMessagesAdapter;
    private RecyclerView mMessagesView;
    private ArrayList<String> mTargetParticipants;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_screen);
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

    //Existing Conversation, so render the messages in the RecyclerView
    private void setupMessagesView() {

        Log.d("Activity", "Conversation exists, setting up view");

        //Hide the "add users" button
        hideAddParticipantsButton();

        //Create the appropriate RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);

        createMessagesAdapter();
        populateToField(mConversation.getParticipants());
    }

    //Takes a String Array of user IDs, finds the display name, and adds them to the "To:" field
    // at the top of the Messages screen
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

        //Uses the helper function to make sure all participant names are appropriately displayed
        // and not cut off due to size constraints
    }

    //If a Conversation ID was not passed into this Activity, we assume that a new Conversation is
    // being created
    private void createNewConversationView() {

        Log.d("Activity", "Creating a new Conversation");

        //Create the appropriate RecyclerView which will be attached to the QueryController when it
        // is created (after the first message is sent and the Conversation is actually created)
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);
    }

    //This is called when there is a valid Conversation to attach the RecyclerView to the appropriate
    // QueryAdapter. Whenever a new Message is sent to the Conversation, the RecyclerView will be updated
    private void createMessagesAdapter() {

        //The Query Adapter drives the RecyclerView, and handles all the heavy lifting of checking 
        // for new Messages, and updating the RecyclerView 
        mMessagesAdapter = new MessageQueryAdapter(getApplicationContext(), LayerImpl.getLayerClient(), mMessagesView, mConversation, this, new QueryAdapter.Callback() {

            public void onItemInserted() {
                //When a new item is inserted into the RecyclerView, scroll to the bottom so the
                // most recent Message is always displayed
                mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
        mMessagesView.setAdapter(mMessagesAdapter);

        //Execute the Query
        mMessagesAdapter.refresh();

        //Start by scrolling to the bottom (newest Message)
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    //You can choose to present additional options when a Message is tapped
    public void onMessageClick(Message message) {

    }

    //You can choose to present additional options when a Message is long tapped
    public boolean onMessageLongClick(Message message) {
        return false;
    }


    //Handle the sendButtona nd Add/Remove Participants button (if displayed)
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
            if (mTargetParticipants.size() > 1) {

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
            friend.setText(ParseImpl.getUsername(friendId));

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
//        findViewById(R.id.action_add_part).setVisibility(View.GONE);
    }

    //When the RecyclerView changes size because of the Soft Keyboard, force scroll to the bottom
    // in order to always show the latest message
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
        getMenuInflater().inflate(R.menu.menu_message_screen, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_part) {
            showParticipantPicker();
        }

        return true;
    }
}