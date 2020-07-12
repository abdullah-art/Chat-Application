package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriensActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private DatabaseReference userRef;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friens);

        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        toolbar=(Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        recyclerView=(RecyclerView)findViewById(R.id.find_friends_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        fetch();
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView username,about;
        CircleImageView imageView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_name);
            about=itemView.findViewById(R.id.user_status);
            imageView=itemView.findViewById(R.id.profile_image);
        }
    }


    private void  fetch(){
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        FirebaseRecyclerOptions<ContactsSet> options=
                new FirebaseRecyclerOptions.Builder<ContactsSet>()
                        .setQuery(query, new SnapshotParser<ContactsSet>() {
                            @NonNull
                            @Override
                            public ContactsSet parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new ContactsSet(snapshot.child("username").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("about").getValue().toString()
                                );
                            }
                        })
                        .build();



        adapter= new FirebaseRecyclerAdapter<ContactsSet, FindFriendsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull ContactsSet model) { holder.username.setText(model.getUsername());
                holder.itemView.findViewById(R.id.buttons_layout).setVisibility(View.GONE);
                holder.about.setText(model.getAbout());
                Picasso.get().load(model.getImage()).into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uid=getRef(position).getKey();
                        Intent intent=new Intent(FindFriensActivity.this,ProfileActivity.class);
                        intent.putExtra("uid",uid);
                        startActivity(intent);
                    }

                }); }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list,parent,false);
                        FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {

        finish();
        Intent intent = new Intent(FindFriensActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
