package com.example.chatbox;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private List<Messages> messageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessagesAdapter(List<Messages> list){
        this.messageList=list;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {
        Messages message=messageList.get(position);
        String senderId=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users")
                .child(message.getFrom());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).into(holder.receiverImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message.getType().equals("text"))
        {
            holder.receiverMsg.setVisibility(View.INVISIBLE);
            holder.receiverImage.setVisibility(View.INVISIBLE);
            holder.senderMsg.setVisibility(View.INVISIBLE);
            if(message.getFrom().equals(senderId))
            {
                holder.senderMsg.setVisibility(View.VISIBLE);
                holder.senderMsg.setText(message.getMessage());
                holder.senderMsg.setBackgroundResource(R.drawable.sender_layout);
                holder.senderMsg.setTextColor(Color.WHITE);
            }
            else{
                holder.senderMsg.setVisibility(View.INVISIBLE);
                holder.receiverMsg.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);
                holder.receiverMsg.setBackgroundResource(R.drawable.receiver_layout);
                holder.receiverMsg.setText(message.getMessage());
                holder.receiverMsg.setTextColor(Color.BLACK);

            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public static class MessagesViewHolder extends RecyclerView.ViewHolder{

        private TextView senderMsg,receiverMsg;
        private CircleImageView receiverImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.sender_messages);
            receiverMsg=itemView.findViewById(R.id.receiver_messages);
            receiverImage=itemView.findViewById(R.id.msg_pic);
        }
    }
}
