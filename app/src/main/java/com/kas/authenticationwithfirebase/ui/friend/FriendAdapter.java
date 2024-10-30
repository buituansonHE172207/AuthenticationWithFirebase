package com.kas.authenticationwithfirebase.ui.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.model.User;

import org.w3c.dom.Text;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<User> friendList;
    private OnFriendClickListener onFriendClickListener;

    public void setOnFriendClickListener(OnFriendClickListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
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

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friend_name);
            friendEmailTextView = itemView.findViewById(R.id.friend_email);
            friendStatusTextView = itemView.findViewById(R.id.friend_status);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFriendClickListener != null) {
                    onFriendClickListener.onFriendClick(friendList.get(position));
                }
            });
        }

        void bind(User friend) {
            friendNameTextView.setText(friend.getUsername());
            friendEmailTextView.setText(friend.getEmail());
            friendStatusTextView.setText(friend.getStatus());
        }
    }
}
