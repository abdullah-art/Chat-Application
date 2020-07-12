package com.example.chatbox;


import android.app.DownloadManager;
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
import android.widget.Toast;

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
public class contacts extends Fragment {

    private View ContactsView;
    private RecyclerView contactsList;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference userRef;


    public contacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         ContactsView=inflater.inflate(R.layout.fragment_contacts, container, false);
         mAuth=FirebaseAuth.getInstance();
         currentUserId=mAuth.getCurrentUser().getUid();
         userRef=FirebaseDatabase.getInstance().getReference().child("Users");
         contactsList=ContactsView.findViewById(R.id.contacts_list);
         contactsList.setLayoutManager(new LinearLayoutManager(getContext()));
         contactsList.setHasFixedSize(true);
        fetch();
         return ContactsView;
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView username,about;
        CircleImageView imageView;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_name);
            about=itemView.findViewById(R.id.user_status);
            imageView=itemView.findViewById(R.id.profile_image);
        }
    }

    private void fetch(){
        Query query= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        FirebaseRecyclerOptions<ContactsSet> options=
                new FirebaseRecyclerOptions.Builder<ContactsSet>()
                .setQuery(query,ContactsSet.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<ContactsSet,ContactsViewHolder>(options) {

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list,parent,false);
                return new ContactsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull ContactsSet contactsSet) {
                contactsViewHolder.itemView.findViewById(R.id.buttons_layout).setVisibility(View.GONE);
                String uid=getRef(i).getKey();
                userRef.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        contactsViewHolder.username.setText(dataSnapshot.child("username").getValue().toString());
                        contactsViewHolder.about.setText(dataSnapshot.child("about").getValue().toString());
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(contactsViewHolder.imageView);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        contactsList.setAdapter(adapter);
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
}
