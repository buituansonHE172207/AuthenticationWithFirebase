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
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private final List<MessageWithUserDetail> messages;
    private final String currentUserId;

    public MessageAdapter(List<MessageWithUserDetail> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageWithUserDetail message = messages.get(position);
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
        MessageWithUserDetail message = messages.get(position);

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
        TextView timestampTextView;

        SentTextViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.sent_text);
            timestampTextView = itemView.findViewById(R.id.tvTimestamp);

        }

        void bind(MessageWithUserDetail message) {
            messageTextView.setText(message.getMessageContent());
            timestampTextView.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    static class ReceivedTextViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;
        TextView senderNameTextView;
        ImageView profileImageView;


        ReceivedTextViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.received_text);
            timestampTextView = itemView.findViewById(R.id.tvTimestamp);
            senderNameTextView = itemView.findViewById(R.id.user_chat_name);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }

        void bind(MessageWithUserDetail message) {
            messageTextView.setText(message.getMessageContent());
            timestampTextView.setText(formatTimestamp(message.getTimestamp()));
            senderNameTextView.setText(message.getUsername());

            // Load profile image using Glide
            Glide
                    .with(itemView.getContext())
                    .load(message.getProfileImageUrl()) // Ensure profile image URL is provided
                    .placeholder(R.drawable.default_avatar) // Placeholder image if URL is missing
                    .into(profileImageView);
        }
    }

    // Image ViewHolder classes
    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageImageView;

        SentImageViewHolder(View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.sent_image);
        }

        void bind(MessageWithUserDetail message) {
            Glide.
                    with(itemView.getContext())
                    .load(message.getMessageContent())
                    .into(messageImageView);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        ImageView messageImageView;

        ReceivedImageViewHolder(View itemView) {
            super(itemView);
            messageImageView = itemView.findViewById(R.id.received_image);
        }

        void bind(MessageWithUserDetail message) {
            Glide.with(itemView.getContext()).load(message.getMessageContent()).into(messageImageView);
        }
    }

    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

}