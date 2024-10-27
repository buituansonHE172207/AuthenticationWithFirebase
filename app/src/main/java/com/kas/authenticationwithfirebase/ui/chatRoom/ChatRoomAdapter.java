package com.kas.authenticationwithfirebase.ui.chatRoom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.model.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms = new ArrayList<>();
    private OnChatRoomClickListener listener;

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

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.bind(chatRoom);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatRoomName;
        private final TextView lastMessage;
        private final TextView lastMessageTimestamp;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomName = itemView.findViewById(R.id.chatRoomName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTimestamp = itemView.findViewById(R.id.lastMessageTimestamp);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onChatRoomClick(chatRooms.get(getAdapterPosition()));
                }
            });
        }

        void bind(ChatRoom chatRoom) {
            chatRoomName.setText(chatRoom.isGroupChat() ? "Group Chat" : "Chat");
            lastMessage.setText(chatRoom.getLastMessage());

            if (chatRoom.getLastMessageTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault());
                String dateString = sdf.format(new Date(chatRoom.getLastMessageTimestamp()));
                lastMessageTimestamp.setText(dateString);
            } else {
                lastMessageTimestamp.setText("");
            }
        }
    }
}
