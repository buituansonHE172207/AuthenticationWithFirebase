package com.kas.authenticationwithfirebase.ui.chatRoom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms = new ArrayList<>();
    private OnChatRoomClickListener listener;
    private final Map<String, Integer> unreadCounts = new HashMap<>();
    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.listener = listener;
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        this.chatRooms.clear();
        this.chatRooms.addAll(chatRooms);
        notifyDataSetChanged();
    }
    public void updateUnreadCount(String chatRoomId, int count) {
        unreadCounts.put(chatRoomId, count);
        for (int i = 0; i < chatRooms.size(); i++) {
            if (chatRooms.get(i).getChatRoomId().equals(chatRoomId)) {
                notifyItemChanged(i, count);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        Integer unreadCount = unreadCounts.get(chatRoom.getChatRoomId());
        holder.bind(chatRoom, unreadCount != null ? unreadCount : 0);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatRoomName;
        private final TextView lastMessage;
        private final TextView lastMessageTimestamp;
        private final TextView unreadCount;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomName = itemView.findViewById(R.id.chatRoomName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTimestamp = itemView.findViewById(R.id.lastMessageTimestamp);
            unreadCount = itemView.findViewById(R.id.unreadCount);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onChatRoomClick(chatRooms.get(getAdapterPosition()));
                }
            });
        }

        void bind(ChatRoom chatRoom,int count) {
            String chatRoomNameText = chatRoom.isGroupChat() ? "Group Chat" : "Chat";
            chatRoomNameText = chatRoomNameText + chatRoom.getChatRoomName();
            chatRoomName.setText(chatRoomNameText);
            lastMessage.setText(chatRoom.getLastMessage());

            if (chatRoom.getLastMessageTimestamp() != null) {
                //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault());
                //String dateString = sdf.format(new Date(chatRoom.getLastMessageTimestamp()));
                //lastMessageTimestamp.setText(dateString);
                lastMessageTimestamp.setText(getRelativeTime(chatRoom.getLastMessageTimestamp()));
            } else {
                lastMessageTimestamp.setText("");
            }
            if (count > 0) {
                unreadCount.setText(String.valueOf(count));
                unreadCount.setVisibility(View.VISIBLE);
            } else {
                unreadCount.setVisibility(View.GONE);
            }
        }
    }
    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) { // less than a minute
            return "Just now";
        } else if (diff < 60 * 60 * 1000) { // less than an hour
            long minutes = diff / (60 * 1000);
            return minutes + " min ago";
        } else if (diff < 24 * 60 * 60 * 1000) { // less than a day
            long hours = diff / (60 * 60 * 1000);
            return hours + " hr ago";
        } else if (diff < 7 * 24 * 60 * 60 * 1000) { // less than a week
            long days = diff / (24 * 60 * 60 * 1000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

}
