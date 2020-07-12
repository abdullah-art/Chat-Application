package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView name,status;
    private CircleImageView profile;
    private String uid,currentUserId;
    private String currentState="not_sent";
    private FirebaseAuth mAuth;
    private Button reqBtn,cancelBtn;
    private DatabaseReference databaseReference,chatReqRef,contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializingFields();
        retrievingData();
        manageChatRequests();
    }

    private void manageChatRequests() {
        if(currentUserId.equals(uid)){
            reqBtn.setVisibility(View.INVISIBLE);
        }
        else{
            chatReqRef.child(currentUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.hasChild(uid))
                                {
                                    String req_status=dataSnapshot.child(uid).child("request_status").getValue().toString();
                                    if(req_status.equals("sent")){
                                        currentState="sent";
                                        reqBtn.setText("Cancel Request");
                                    }
                                    else if(req_status.equals("received"))
                                    {
                                        reqBtn.setText("Accept Request");
                                        currentState="received";
                                        cancelBtn.setEnabled(true);
                                        cancelBtn.setVisibility(View.VISIBLE);
                                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                cancelRequest();
                                                reqBtn.setText("Send Message");
                                                reqBtn.setEnabled(true);
                                                cancelBtn.setEnabled(false);
                                                cancelBtn.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                            }
                            else{
                                contactsRef.child(currentUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    if(dataSnapshot.hasChild(uid))
                                                    {
                                                        currentState="friends";
                                                        reqBtn.setEnabled(true);
                                                        reqBtn.setText("Remove Friend");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            reqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reqBtn.setEnabled(false);
                    if(currentState.equals("not_sent")){
                        sendRequest();
                    }
                    else if(currentState.equals("sent")){
                        cancelRequest();
                    }
                    else if(currentState.equals("received")){
                        updateContacts();
                    }
                    else if(currentState.equals("friends")){
                        removeContact();
                    }
                }
            });
        }
    }
    private void removeContact(){
        contactsRef.child(currentUserId).child(uid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        contactsRef.child(uid).child(currentUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        currentState="not_sent";
                                        reqBtn.setEnabled(true);
                                        reqBtn.setText("Send Message");

                                        cancelBtn.setVisibility(View.INVISIBLE);
                                        cancelBtn.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void updateContacts(){
        contactsRef.child(currentUserId).child(uid)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           contactsRef.child(uid).child(currentUserId)
                                   .child("Contacts").setValue("saved")
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               chatReqRef.child(currentUserId).child(uid)
                                                       .removeValue()
                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {
                                                               chatReqRef.child(uid).child(currentUserId)
                                                                       .removeValue()
                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                currentState="friends";
                                                                                reqBtn.setText("Remove Friend");
                                                                                reqBtn.setEnabled(true);
                                                                                cancelBtn.setEnabled(false);
                                                                                cancelBtn.setVisibility(View.INVISIBLE);
                                                                           }
                                                                       });
                                                           }
                                                       });
                                           }
                                       }
                                   });
                       }
                    }
                });
    }

    private void cancelRequest(){
        chatReqRef.child(currentUserId).child(uid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        chatReqRef.child(uid).child(currentUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        currentState="not_sent";
                                        reqBtn.setEnabled(true);
                                        reqBtn.setText("Send Message");

                                        cancelBtn.setVisibility(View.INVISIBLE);
                                        cancelBtn.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void sendRequest() {

        chatReqRef.child(currentUserId).child(uid)
                .child("request_status").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatReqRef.child(uid).child(currentUserId)
                                    .child("request_status").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState="sent";
                                                reqBtn.setText("Cancel Request");
                                                reqBtn.setEnabled(true);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void retrievingData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    name.setText(dataSnapshot.child("username").getValue().toString());
                    status.setText(dataSnapshot.child("about").getValue().toString());
                    String profileUrl=dataSnapshot.child("image").getValue().toString();
                    Uri uri=Uri.parse(profileUrl);
                    Picasso.get().load(uri).into(profile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializingFields() {
        name=findViewById(R.id.name);
        reqBtn=findViewById(R.id.req_btn);
        cancelBtn=findViewById(R.id.cancel_btn);
        status=findViewById(R.id.status);
        profile=findViewById(R.id.profile_picture);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        uid= getIntent().getExtras().get("uid").toString();
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        chatReqRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
    }
}
