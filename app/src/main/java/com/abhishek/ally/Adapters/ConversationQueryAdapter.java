package com.abhishek.ally.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abhishek.ally.Layer.LayerImpl;
import com.abhishek.ally.Parse.ParseImpl;
import com.abhishek.ally.Views.TextViewPlus;
import com.layer.ally.R;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.parse.ParseUser;

import java.util.List;

/*
 * ConversationQueryAdapter.java
 * Drives the RecyclerView in the ConversationsActivity class. Shows a list of conversations sorted
 *  by the last message received. For each Conversation, it shows the participants (not including the
 *  locally authenticated user), the time of the last message received, and the contents of the last
 *  message.
 *
 *  This is just one possible implementation. You can edit the conversation_item.xml view to change
 *   what is shown for each Conversation, including adding icons for individual or group messages,
 *   indicating whether the latest message in a Conversation is unread, etc.
 */


public class ConversationQueryAdapter extends QueryAdapter<Conversation, ConversationQueryAdapter.ViewHolder> {

    //Inflates the view associated with each Conversation object returned by the Query
    private final LayoutInflater mInflater;

    //Handle the callbacks when the Conversation item is actually clicked. In this case, the
    // ConversationsActivity class implements the ConversationClickHandler
    private final ConversationClickHandler mConversationClickHandler;

    //Constructor for the ConversationQueryAdapter
    //Sorts all conversations by last message received (ie, downloaded to the device)
    public ConversationQueryAdapter(Context context, LayerClient client, ConversationClickHandler conversationClickHandler, Callback callback) {
        super(client, Query.builder(Conversation.class)
                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                .build(), callback);

        //Sets the LayoutInflator and Click callback handler
        mInflater = LayoutInflater.from(context);
        mConversationClickHandler = conversationClickHandler;
    }

    //When a new Conversation is created (ie, either locally, or by another user), a new ViewHolder is created
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //The conversation_item is just an example view you can use to display each Conversation in a list
        View itemView = mInflater.inflate(R.layout.conversation_item, null);

        //Tie the view elements to the fields in the actual view after it has been created
        ViewHolder holder = new ViewHolder(itemView, mConversationClickHandler);
        holder.participants = (TextViewPlus) itemView.findViewById(R.id.participants);
        holder.time = (TextViewPlus) itemView.findViewById(R.id.time);
        holder.lastMsgContent = (TextViewPlus) itemView.findViewById(R.id.message);
        holder.textIcon = (TextViewPlus) itemView.findViewById(R.id.text_icon);


        return holder;
    }

    //After the ViewHolder is created, we need to populate the fields with information from the Conversation
    public void onBindViewHolder(ViewHolder viewHolder, Conversation conversation) {
        if (conversation == null) {
            // If the item no longer exists, the ID probably migrated.
            refresh();
            return;
        }

        viewHolder.conversation = conversation;

        //Go through all the User IDs in the Conversation and find the matching human readable
        // handles from Parse
        String participants = "";
        List<String> users = conversation.getParticipants();
        for (int i = 0; i < users.size(); i++) {
            if (!users.get(i).equals(ParseUser.getCurrentUser().getObjectId())) {
                //Format the String so there is a comma after every username
                if (participants.length() > 0)
                    participants += ", ";

                //Add the human readable username to the String
                participants += ParseImpl.getName(users.get(i));
            }
        }
        viewHolder.participants.setText(participants);
        viewHolder.textIcon.setText(participants.substring(0, 1));

        //Grab the last message in the conversation and show it in the format "sender: last message content"
        Message message = conversation.getLastMessage();
        if (message != null) {
            viewHolder.lastMsgContent.setText(LayerImpl.getMessageText(message).replaceAll("\n", " "));
        } else {
            viewHolder.lastMsgContent.setVisibility(View.GONE);
        }

        //Draw the date the last message was received (downloaded from the server)
        viewHolder.time.setText(LayerImpl.getReceivedAtTime(message));
    }

    //This example app only has one kind of view type, but you could support different TYPES of
    // Conversations if you were so inclined
    public int getItemViewType(int i) {
        return 1;
    }

    public static interface ConversationClickHandler {
        public void onConversationClick(Conversation conversation);

        public boolean onConversationLongClick(Conversation conversation);
    }

    //The fields in the ViewHolder reflect the conversation_item view
    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final ConversationClickHandler conversationClickHandler;
        public TextViewPlus participants;
        public TextViewPlus time;
        public TextViewPlus lastMsgContent;
        public TextViewPlus textIcon;
        public Conversation conversation;

        //Registers the click listener callback handler
        public ViewHolder(View itemView, ConversationClickHandler conversationClickHandler) {
            super(itemView);
            this.conversationClickHandler = conversationClickHandler;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        //Execute the callback when the conversation is clicked
        public void onClick(View v) {
            conversationClickHandler.onConversationClick(conversation);
        }

        //Execute the callback when the conversation is long-clicked
        public boolean onLongClick(View v) {
            return conversationClickHandler.onConversationLongClick(conversation);
        }
    }
}