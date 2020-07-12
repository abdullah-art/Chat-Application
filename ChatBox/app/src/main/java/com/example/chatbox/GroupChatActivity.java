package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private ImageButton imageButton;
    private ScrollView scrollView;
    private TextView displayChat;
    private EditText chatText;
    String groupName;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,groupNameRef,groupMsgKeyRef;
    String currentUserId;
    String currentUserName;
    String currentDate;
    String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat2);
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        databaseReference=FirebaseDatabase.getInstance().getReference();
        groupName=getIntent().getExtras().get("groupName").toString();

        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        InitializeFields();
        getUserInfo();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDatabase();
                chatText.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
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

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String msgDate=(String)((DataSnapshot)iterator.next()).getValue();
            String msg=(String)((DataSnapshot)iterator.next()).getValue();
            String msgUser=(String)((DataSnapshot)iterator.next()).getValue();
            String msgTime=(String)((DataSnapshot)iterator.next()).getValue();
            displayChat.append(msgUser + ": \n" + msg +"\n" +msgTime+"     "+msgDate+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }


    }

    private void saveMessageToDatabase() {
        String message=chatText.getText().toString();
        String uniqueKey=groupNameRef.push().getKey();
        if(!TextUtils.isEmpty(message))
        {
            Calendar  calendar=Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
            currentDate =dateFormat.format(calendar.getTime());

            Calendar  calendar1=Calendar.getInstance();
            SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
            currentTime =timeFormat.format(calendar.getTime());

            groupMsgKeyRef=groupNameRef.child(uniqueKey);
            HashMap<String,Object> msgInfo=new HashMap<>();
            msgInfo.put("name",currentUserName);
            msgInfo.put("message",message);
            msgInfo.put("date",currentDate);
            msgInfo.put("time",currentTime);

            groupMsgKeyRef.updateChildren(msgInfo);


        }
    }

    private void getUserInfo() {
        databaseReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        toolbar=(Toolbar) findViewById(R.id.group_chat_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        imageButton=(ImageButton)findViewById(R.id.send_msg_btn);
        chatText=(EditText) findViewById(R.id.group_chat_typing);
        displayChat=(TextView) findViewById(R.id.group_chat_text);
        scrollView=(ScrollView)findViewById(R.id.scroll_view);
    }
}
