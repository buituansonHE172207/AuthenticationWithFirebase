package com.kas.authenticationwithfirebase.ui.chatRoom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms = new ArrayList<>();
    private boolean hideButton = true; // Default value is true
    private OnChatRoomClickListener clickListener;
    private OnDeleteChatRoomClickListener deleteListener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public interface OnDeleteChatRoomClickListener {
        void onDeleteChatRoomClick(ChatRoom chatRoom);
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteChatRoomClickListener(OnDeleteChatRoomClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        this.chatRooms.clear();
        this.chatRooms.addAll(chatRooms);
        notifyDataSetChanged();
    }

    // Method to change the visibility of the button
    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
        notifyDataSetChanged(); // Refresh the adapter to apply changes
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        holder.bind(chatRooms.get(position));
        if (hideButton) {
            holder.deleteChatButton.setVisibility(View.GONE); // Hide the button
        } else {
            holder.deleteChatButton.setVisibility(View.VISIBLE); // Show the button
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatRoomName;
        private final TextView lastMessage;
        private final TextView lastMessageTimestamp;
        private final Button deleteChatButton;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomName = itemView.findViewById(R.id.chatRoomName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTimestamp = itemView.findViewById(R.id.lastMessageTimestamp);
            deleteChatButton = itemView.findViewById(R.id.clearChatButton);

            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onChatRoomClick(chatRooms.get(getAdapterPosition()));
                }
            });

            deleteChatButton.setOnClickListener(v -> {
                if (deleteListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    ChatRoom chatRoom = chatRooms.get(getAdapterPosition());
                    // Call deleteMessages method to clear the messages in the chat room
                    deleteListener.onDeleteChatRoomClick(chatRoom);
                    Toast.makeText(itemView.getContext(), "Clearing messages in chat room...", Toast.LENGTH_SHORT).show();
                }
            });
        }


        void bind(ChatRoom chatRoom) {
            chatRoomName.setText(chatRoom.isGroupChat() ? "Group: " + chatRoom.getChatRoomName() : chatRoom.getChatRoomName());
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
