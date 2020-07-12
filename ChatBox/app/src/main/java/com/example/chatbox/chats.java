package com.example.chatbox;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class chats extends Fragment {

    private View chatFragmentView;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference userRef;
    public chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth=FirebaseAuth.getInstance();
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId=mAuth.getCurrentUser().getUid();
        chatFragmentView= inflater.inflate(R.layout.fragment_chats2, container, false);
        recyclerView=chatFragmentView.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetch();
        return chatFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private static class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView username,about;
        CircleImageView imageView;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_name);
            about=itemView.findViewById(R.id.user_status);
            imageView=itemView.findViewById(R.id.profile_image);
        }
    }

    private void fetch(){
    Query query= FirebaseDatabase.getInstance().getReference().child("Contacts")
            .child(currentUserId);

        FirebaseRecyclerOptions<ContactsSet> options=
                new FirebaseRecyclerOptions.Builder<ContactsSet>()
                .setQuery(query,ContactsSet.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<ContactsSet,ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i, @NonNull ContactsSet contactsSet) {
                chatViewHolder.itemView.findViewById(R.id.buttons_layout).setVisibility(View.GONE);
                final String uid = getRef(i).getKey();
                  userRef.child(uid).addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(final DataSnapshot dataSnapshot) {
                          if (dataSnapshot.exists()) {
                              chatViewHolder.username.setText(dataSnapshot.child("username").getValue().toString());
                              chatViewHolder.about.setText("Last Seen"+"\n" + "Date  Time");
                              Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(chatViewHolder.imageView);

                              chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                        Intent intent=new Intent(getContext(),ChatActivity.class);
                                        intent.putExtra("uid",uid);
                                        intent.putExtra("name",dataSnapshot.child("username").getValue().toString());
                                        intent.putExtra("profile_pic",dataSnapshot.child("image").getValue().toString());
                                        startActivity(intent);
                                  }
                              });
                          }

                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list,parent,false);
                return new ChatViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
