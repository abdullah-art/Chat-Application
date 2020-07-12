package com.example.chatbox;


import android.app.DownloadManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestView;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,contactsRef,chatReqRef;
    private String currentUserId;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatReqRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        requestView= inflater.inflate(R.layout.fragment_request, container, false);
        recyclerView=requestView.findViewById(R.id.request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetch();
        return requestView;
    }

    private static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView username,about;
        CircleImageView imageView;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_name);
            about=itemView.findViewById(R.id.user_status);
            imageView=itemView.findViewById(R.id.profile_image);
        }
    }

    private void fetch(){
        Query query = FirebaseDatabase.getInstance().getReference().child("Chat Requests")
                .child(currentUserId);

        FirebaseRecyclerOptions<ContactsSet> options=
                new FirebaseRecyclerOptions.Builder<ContactsSet>()
                .setQuery(query,ContactsSet.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<ContactsSet,RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull ContactsSet contactsSet) {
                final String uid=getRef(i).getKey();
                DatabaseReference reqStatusRef=getRef(i).child("request_status").getRef();

                reqStatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String status=dataSnapshot.getValue().toString();
                            if(status.equals("received"))
                            {
                                userRef.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            requestViewHolder.username.setText(dataSnapshot.child("username").getValue().toString());
                                            requestViewHolder.about.setText(dataSnapshot.child("about").getValue().toString());
                                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(requestViewHolder.imageView);


                                            requestViewHolder.itemView.findViewById(R.id.reject_btn).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
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
                                                                                    Toast.makeText(getContext(), "Request Rejected", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                }
                                            });

                                            requestViewHolder.itemView.findViewById(R.id.confirm_btn).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
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
                                                                                                                            Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list,parent,false);
                return new RequestViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }
}
