package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String peerUserId,peerUserName,peerUserImage;
    private TextView username,lastseen;
    private CircleImageView image;
    private Toolbar chatToolbar;
    private EditText message;
    private ImageButton sendBtn;
    private String currentUserId;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessagesAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        peerUserId=getIntent().getExtras().get("uid").toString();
        peerUserName=getIntent().getExtras().get("name").toString();
        peerUserImage=getIntent().getExtras().get("profile_pic").toString();
        InitializingFields();

        username.setText(peerUserName);
        Picasso.get().load(peerUserImage).into(image);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(currentUserId)
                .child(peerUserId)
               .addChildEventListener(new ChildEventListener() {
                   @Override
                   public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                   }

                   @Override
                   public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                   }

                   @Override
                   public void onChildRemoved(DataSnapshot dataSnapshot) {

                   }

                   @Override
                   public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

    }

    private void sendMessage() {
        String msg=message.getText().toString();
        if(TextUtils.isEmpty(msg)){
            Toast.makeText(this, "Empty Field", Toast.LENGTH_SHORT).show();
        }
        else{
            String currentUserRef="Messages/" +currentUserId+"/"+peerUserId;
            String peerUserRef="Messages/"+peerUserId+"/"+currentUserId;

            DatabaseReference uniqueKeyRef=rootRef.child(currentUserId).child(peerUserId).push();
            String uniqueKey=uniqueKeyRef.getKey();

            Map body=new HashMap();
            body.put("message",msg);
            body.put("from",currentUserId);
            body.put("to",peerUserId);
            body.put("type","text");

            Map details=new HashMap();
            details.put(currentUserRef+"/"+uniqueKey,body);
            details.put(peerUserRef+"/"+uniqueKey,body);

            rootRef.updateChildren(details).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Something Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        message.setText("");
    }

    private void InitializingFields() {
        chatToolbar= findViewById(R.id.chat_tool_bar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater LayoutInflater =
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v=LayoutInflater.inflate(R.layout.custom_chat_layout,null);
        actionBar.setCustomView(v);
        username=findViewById(R.id.peer_username);
        lastseen=findViewById(R.id.peer_user_lastseen);
        image=findViewById(R.id.peer_user_image);
        message=findViewById(R.id.chat_text);
        sendBtn=findViewById(R.id.send_chat_btn);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        adapter=new MessagesAdapter(messagesList);
        recyclerView=findViewById(R.id.chat_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
