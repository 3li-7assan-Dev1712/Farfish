package com.example.friendlychat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    /* interface for listening to touching*/
    public interface MessageClick{
        void onMessageClick(View view, int position);
    }
    static MessageClick mMessageInterface;
    private static final String TAG = MessagesAdapter.class.getSimpleName();
    private Context mContext;
    private List<Message> messages;

    private String mCurrentUserId;
    private static final int USE_SENDER_BACKGROUND = 1;
    private static final int USE_SENDER_BACKGROUND_IMG = 3;
    private static final int USE_LOCAL_BACKGROUND = 0;
    private static final int USE_LOCAL_BACKGROUND_IMG = 2;


    public MessagesAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    public MessagesAdapter(Context mContext, List<Message> messages, MessageClick messageInterface) {
        this.mContext = mContext;
        this.messages = messages;
        mMessageInterface = messageInterface;
        mCurrentUserId = MessagesPreference.getUserId(mContext);
    }

    @Override
    public int getItemViewType(int position) {
        String senderId = messages.get(position).getSenderId();
        String photoUrl = messages.get(position).getPhotoUrl();
        if (messages == null){
            throw new NullPointerException("From getItemViewType message is null");
        }
        if (senderId == null || photoUrl == null) {
            return USE_SENDER_BACKGROUND;
        }

        Log.d(TAG, "getItemViewType: senderId: " + senderId);
        Log.d(TAG, "getItemViewType: photoUrl: " + photoUrl);
        if (useCurrentMessageBackground(senderId) && !photoUrl.equals("")){
            return USE_LOCAL_BACKGROUND_IMG;
        }else if (useCurrentMessageBackground(senderId) && photoUrl.equals("")){
            return USE_LOCAL_BACKGROUND;
        }else if (!useCurrentMessageBackground(senderId) && photoUrl.equals("")){
            return USE_SENDER_BACKGROUND;
        }else {
            return USE_SENDER_BACKGROUND_IMG;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder");
        if (viewType == USE_LOCAL_BACKGROUND){
            View localMessage = LayoutInflater.from(mContext).inflate(R.layout.local_message_view_holder, parent, false);
            return new MessageViewHolder(localMessage);
        }else  if (viewType == USE_LOCAL_BACKGROUND_IMG){
            View sendImageView = LayoutInflater.from(mContext).inflate(R.layout.send_image_view_holder, parent, false);
            return new MessageViewHolder(sendImageView);
        }else if (viewType == USE_SENDER_BACKGROUND){
            View receivedMessage = LayoutInflater.from(mContext).inflate(R.layout.message_view_holder, parent, false);
            return new MessageViewHolder(receivedMessage);
        }else{
            View receivedImageView = LayoutInflater.from(mContext).inflate(R.layout.receive_image_view_holder, parent, false);
            return new MessageViewHolder(receivedImageView);
        }

    }


    public boolean useCurrentMessageBackground(String senderId) {
        String currentUserId = MessagesPreference.getUserId(mContext);
        return currentUserId.equals(senderId);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        if (messages != null && messages.size() != 0){

            Message message = messages.get(position);
            String name =message.getSenderName();
            String messageText = message.getText();
            String photoUrl = message.getPhotoUrl();
            long dateFromServer = message.getTimestamp();
            if (photoUrl != null && !photoUrl.equals("")){
                Log.d(TAG, "photoUrl is: "+photoUrl);
                Picasso.get().load(photoUrl).placeholder(R.drawable.ic_baseline_emoji_emotions_24).into(holder.imageView);
                ViewCompat.setTransitionName(holder.imageView, String.valueOf(dateFromServer));

            }else {
                holder.messageTextView.setText(messageText);
            }/*
            long messageTime = message.getTimestamp();
            Date date = new Date(messageTime);*/
            if (name == null)
                holder.authorName.setText("null");
            else
                holder.authorName.setText(name);

            long currentTime = System.currentTimeMillis();
            if (dateFromServer == currentTime)
                Log.d(TAG, "There are the same");
            SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String readableDate = d.format(dateFromServer);
            Log.d(TAG, "readable date in Adapter is : " + readableDate);
            holder.timeMessageTextView.setText(readableDate);

            if (message.getSenderId().equals(mCurrentUserId)) {
                if (message.getIsRead()) {
                    Log.d(TAG, "onBindViewHolder: send the indicator as read");
                    holder.isReadIndicatorImageView.setImageResource(
                            R.drawable.ic_done_all_24
                    );
                }else{
                    holder.isReadIndicatorImageView.setImageResource(
                            R.drawable.ic_done_all_black
                    );
                }

            }
        }
    }



    @Override
    public int getItemCount() {
        if (messages != null){
            return messages.size();
        }else
            return 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView messageTextView;
        private TextView authorName;
        private ImageView imageView;
        private TextView timeMessageTextView;
        private ImageView isReadIndicatorImageView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            imageView = itemView.findViewById(R.id.photoImageView);
            authorName = itemView.findViewById(R.id.nameTextView);
            timeMessageTextView = itemView.findViewById(R.id.timeMessage);
            isReadIndicatorImageView = itemView.findViewById(R.id.isReadIconIndicator);
            imageView.setOnClickListener(view -> {
                mMessageInterface.onMessageClick(view, getBindingAdapterPosition());
            });
        }

    }

}
