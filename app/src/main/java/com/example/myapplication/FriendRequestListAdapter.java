package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendRequestListAdapter extends RecyclerView.Adapter<FriendRequestListAdapter.ViewHolder> {

    private List<UserBean> friendRequestList;
    private OnItemClickListener listener;


    public FriendRequestListAdapter(List<UserBean> friendRequestList, OnItemClickListener listener) {
        this.friendRequestList = friendRequestList;
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserBean request = friendRequestList.get(position);
        holder.bind(request, listener);
    }


    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private final ImageView acceptImageView;
        private final ImageView rejectImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            acceptImageView = itemView.findViewById(R.id.acceptImageView);
            rejectImageView = itemView.findViewById(R.id.rejectImageView);
        }

        public void bind(final UserBean request, final OnItemClickListener listener) {
            usernameTextView.setText(request.getUsername() + " want add you as friend");
            acceptImageView.setOnClickListener(v -> listener.onAccept(request));
            rejectImageView.setOnClickListener(v -> listener.onReject(request));
        }
    }

    public interface OnItemClickListener {
        void onAccept(UserBean request);
        void onReject(UserBean request);
    }
}
