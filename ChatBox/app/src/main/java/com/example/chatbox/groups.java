package com.example.chatbox;


import android.content.Intent;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class groups extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View groupFragmentView;
    private ArrayList<String> groupList=new ArrayList<String>();

    private DatabaseReference groupRef;


    public groups() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        retrievingGroups();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupRef= FirebaseDatabase.getInstance().getReference();
        groupFragmentView= inflater.inflate(R.layout.fragment_groups2, container, false);
        InitializeFiels();
        return groupFragmentView;
    }

    private void retrievingGroups() {

        groupRef.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator iterator=dataSnapshot.getChildren().iterator();
                Set<String> set=new HashSet<>();
                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                groupList.clear();
                groupList.addAll(set);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFiels() {
        mRecyclerView=groupFragmentView.findViewById(R.id.recycler_view);
        mLayoutManager=new LinearLayoutManager(getContext());
        mAdapter=new RecyclerViewAdapter(groupList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String currentGroupName=groupList.get(position);
                Intent groupActivityIntent=new Intent(getContext(),GroupChatActivity.class);
                groupActivityIntent.putExtra("groupName",currentGroupName);
                startActivity(groupActivityIntent);
            }
        });

    }


}
