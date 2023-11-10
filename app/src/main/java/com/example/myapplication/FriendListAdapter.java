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

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<UserBean> friendRequestList;
    private FriendClickListener clickListener;

    // 构造函数
    public FriendListAdapter(List<UserBean> friendRequestList, FriendClickListener clickListener) {
        this.friendRequestList = friendRequestList;
        this.clickListener = clickListener;
    }

    public interface FriendClickListener {
        void onFriendClick(UserBean friend);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friends, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserBean request = friendRequestList.get(position);
        holder.bind(request);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null) {
                    clickListener.onFriendClick(request);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private final TextView userName;
        private final CircleImageView profileImage;

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            userName = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            String currentUserId = mAuth.getCurrentUser().getUid();

            db.collection("images").document(currentUserId)
                    .get() // Changed to get() for a single read instead of a real-time listener
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Fetch profile image
                                String myProfileImage = documentSnapshot.getString("uri");
                                if (myProfileImage != null && !myProfileImage.isEmpty()) {
                                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(profileImage);

                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ProfileActivity", "Error fetching profile image", e);
                        }
                    });
        }

        public void bind(final UserBean request) {
            usernameTextView.setText(request.getUsername());
        }
    }
}
