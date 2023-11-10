package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_bubbles, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView theirMessageView;
        private final TextView myMessageView;

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;

        public ViewHolder(View itemView) {
            super(itemView);
            theirMessageView = itemView.findViewById(R.id.tvTheirMsg);
            myMessageView = itemView.findViewById(R.id.tvMyMsg);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            currentUserId = mAuth.getCurrentUser().getUid();
        }

        public void bind(final Message message) {
            if(message.senderID.equals(currentUserId)) {
                myMessageView.setText(message.messageTxt);
                theirMessageView.setVisibility(View.INVISIBLE);
            } else {
                theirMessageView.setText(message.messageTxt);
                myMessageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
