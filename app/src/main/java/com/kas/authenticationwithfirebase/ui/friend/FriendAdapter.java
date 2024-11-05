package com.kas.authenticationwithfirebase.ui.friend;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.User;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<User> friendList;
    private OnFriendClickListener onFriendClickListener;
    private OnFriendButtonClickListener onFriendButtonClickListener;

    public void setOnFriendClickListener(OnFriendClickListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }
    public void setOnFriendButtonClickListener(OnFriendButtonClickListener onFriendButtonClickListener) {
        this.onFriendButtonClickListener = onFriendButtonClickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendList != null ? friendList.size() : 0;
    }

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
    }
    public interface OnFriendButtonClickListener{
        void onFriendButtonClick(User friend);
    }

    public void updateFriendList(List<User> newFriendList) {
        if (friendList == null) {
            friendList = newFriendList;
            notifyItemRangeInserted(0, newFriendList.size());
        } else {
            // Handle more specific updates here
            for (int i = 0; i < newFriendList.size(); i++) {
                if (!friendList.contains(newFriendList.get(i))) {
                    friendList.add(i, newFriendList.get(i));
                    notifyItemInserted(i);
                } else {
                    friendList.set(i, newFriendList.get(i));
                    notifyItemChanged(i);
                }
            }
            for (int i = friendList.size() - 1; i >= newFriendList.size(); i--) {
                friendList.remove(i);
                notifyItemRemoved(i);
            }
        }
    }


    class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView friendNameTextView;
        private TextView friendEmailTextView;
        private TextView friendStatusTextView;
        private ImageView avatar;
        private View onlineIndicator;
        private ImageButton viewChatButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friend_name);
            friendEmailTextView = itemView.findViewById(R.id.friend_email);
            friendStatusTextView = itemView.findViewById(R.id.friend_status);
            avatar = itemView.findViewById(R.id.avatar);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
            viewChatButton = itemView.findViewById(R.id.btn_view_chat);
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFriendClickListener != null) {
                    onFriendClickListener.onFriendClick(friendList.get(position));
                }
            });
            // Handle chat button click
            viewChatButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFriendClickListener != null) {
                    User friend = friendList.get(position);
                    onFriendButtonClickListener.onFriendButtonClick(friend);
                }
            });
        }
        void bind(User friend) {
            friendNameTextView.setText(friend.getUsername());
            //friendEmailTextView.setText(friend.getEmail());
            friendEmailTextView.setText("Email: " + "mockupemail@gmail.com");
            //Log.d("friendStatus",friend.getEmail());
            friendStatusTextView.setText(friend.getStatus());

            if ("ONLINE".equalsIgnoreCase(friend.getStatus())) {
                onlineIndicator.setVisibility(View.VISIBLE);
                friendStatusTextView.setText(friend.getStatus());
                friendStatusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.online_status_color));
            } else {
                onlineIndicator.setVisibility(View.GONE);
                friendStatusTextView.setText(friend.getStatus());
                friendStatusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.text_color));
            }

            // Load profile image
            Glide.with(itemView.getContext())
                    .load(friend.getProfileImageUrl())
                    .placeholder(R.drawable.default_avatar) // Placeholder image
                    .into(avatar);
        }

    }
}
