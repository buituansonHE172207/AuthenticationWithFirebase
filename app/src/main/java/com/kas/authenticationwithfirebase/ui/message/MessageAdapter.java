package com.kas.authenticationwithfirebase.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private final List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);

        if (isSent) {
            if ("image".equals(message.getMessageType())) {
                return VIEW_TYPE_SENT_IMAGE;
            } else {
                return VIEW_TYPE_SENT_TEXT;
            }
        } else {
            if ("image".equals(message.getMessageType())) {
                return VIEW_TYPE_RECEIVED_IMAGE;
            } else {
                return VIEW_TYPE_RECEIVED_TEXT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT_TEXT) {
            View view = inflater.inflate(R.layout.item_sent_text, parent, false);
            return new SentTextViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED_TEXT) {
            View view = inflater.inflate(R.layout.item_received_text, parent, false);
            return new ReceivedTextViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            View view = inflater.inflate(R.layout.item_sent_image, parent, false);
            return new SentImageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_received_image, parent, false);
            return new ReceivedImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentTextViewHolder) {
            ((SentTextViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedTextViewHolder) {
            ((ReceivedTextViewHolder) holder).bind(message);
        } else if (holder instanceof SentImageViewHolder) {
            ((SentImageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedImageViewHolder) {
            ((ReceivedImageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Text ViewHolder classes
    static class SentTextViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        SentTextViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.sent_text);
        }

        void bind(Message message) {
            messageTextView.setText(message.getMessageContent());
        }
    }

    static class ReceivedTextViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        ReceivedTextViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.received_text);
        }

        void bind(Message message) {
            messageTextView.setText(message.getMessageContent());
        }
    }

    // Image ViewHolder classes
    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageImageView;

        SentImageViewHolder(View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.sent_image);
        }

        void bind(Message message) {
            Glide.with(itemView.getContext()).load(message.getMessageContent()).into(messageImageView);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageImageView;

        ReceivedImageViewHolder(View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.received_image);
        }

        void bind(Message message) {
            Glide.with(itemView.getContext()).load(message.getMessageContent()).into(messageImageView);
        }
    }
}