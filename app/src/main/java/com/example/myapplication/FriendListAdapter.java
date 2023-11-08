package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<UserBean> friendRequestList;

    // 构造函数
    public FriendListAdapter(List<UserBean> friendRequestList) {
        this.friendRequestList = friendRequestList;

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
    }


    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private final TextView userName;
        private final ImageView profileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            userName = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        public void bind(final UserBean request) {
            usernameTextView.setText(request.getUsername());
        }
    }
}
