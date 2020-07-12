package com.example.chatbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GroupsViewHolder> {

    private ArrayList<String> groupList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
         void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder{

        private TextView group_name;
        public GroupsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            group_name=itemView.findViewById(R.id.group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null)
                    {
                        int postion=getAdapterPosition();
                        if(postion!=RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(postion);
                        }
                    }
                }
            });
        }
    }

    public RecyclerViewAdapter(ArrayList<String> list){
        groupList=list;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list,parent,false);
        GroupsViewHolder groupsViewHolder=new GroupsViewHolder(v,mListener);
        return groupsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsViewHolder holder, int position) {
        String name=groupList.get(position);
        holder.group_name.setText(name);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}
