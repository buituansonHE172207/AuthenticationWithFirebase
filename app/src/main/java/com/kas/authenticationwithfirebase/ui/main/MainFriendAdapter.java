package com.kas.authenticationwithfirebase.ui.main;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.User;

import java.util.List;

public class MainFriendAdapter extends RecyclerView.Adapter<MainFriendAdapter.MainFriendViewHolder> {
    private List<User> friendList;
    private OnFriendClickListener onFriendClickListener;

    public void setOnFriendClickListener(OnFriendClickListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public MainFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_main, parent, false);
        return new MainFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainFriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        int size = friendList != null ? friendList.size() : 0;
        return size;
    }

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
    }

    class MainFriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView friendNameTextView;
        //private TextView friendStatusTextView;
        private View onlineIndicator;
        public MainFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            friendNameTextView = itemView.findViewById(R.id.friend_name);
            //friendStatusTextView = itemView.findViewById(R.id.friend_status);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFriendClickListener != null) {
                    User clickedFriend = friendList.get(position);
                    onFriendClickListener.onFriendClick(clickedFriend);
                }
            });
        }

        void bind(User friend) {
            Glide.with(itemView.getContext())
                    .load(friend.getProfileImageUrl())
                    .placeholder(R.drawable.default_avatar)
                    .into(avatar);

            friendNameTextView.setText(friend.getUsername());
            //friendStatusTextView.setText(friend.getStatus());
            if ("ONLINE".equalsIgnoreCase(friend.getStatus())) {
                onlineIndicator.setVisibility(View.VISIBLE);
            } else {
                onlineIndicator.setVisibility(View.GONE);
            }
        }
    }
}
