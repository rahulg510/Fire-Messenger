package com.fire.messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fire.messenger.chat.MessageAdapter;
import com.fire.messenger.chat.MessageObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    ArrayList<MessageObject> messageList;

    String chatID;

    DatabaseReference mChatDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatID = getIntent().getExtras().getString("chatID");

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).push();

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });
        initializeRecyclerView();
        getChatMessages();
    }

    private void getChatMessages() {
       mChatDb.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               if(dataSnapshot.exists()){
                   String text = "",
                           creatorID = "";

                   if(dataSnapshot.child("text").getValue() != null){
                       text = dataSnapshot.child("text").getValue().toString();
                   }
                   if(dataSnapshot.child("creator").getValue() != null){
                       creatorID = dataSnapshot.child("creator").getValue().toString();
                   }

                   MessageObject mMessage = new MessageObject(dataSnapshot.getKey(),creatorID,text);
                   //add the new message to messageList
                   messageList.add(mMessage);
                   //scroll the chat view to the latest message
                   mChatLayoutManager.scrollToPosition(messageList.size()-1);
                   //notify adapter that a new message was added
                   mChatAdapter.notifyDataSetChanged();
               }
           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }


    /**
     * adding a message to db and send to the user.
     */
    private void sendMessage(){
        EditText mMessage = findViewById(R.id.message1);
        if(!mMessage.getText().toString().isEmpty()){
            DatabaseReference newMessageDb = mChatDb.push();
            Map newMessageMap  = new HashMap<>();
            newMessageMap.put("text", mMessage.getText().toString());
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

            newMessageDb.updateChildren(newMessageMap);

        }
        //clear up the message text box

        mMessage.setText(null);
    }

    private void initializeRecyclerView() {
        messageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        //scrolls smoothly
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        //layout for the list
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChat.setAdapter(mChatAdapter);
    }
}
