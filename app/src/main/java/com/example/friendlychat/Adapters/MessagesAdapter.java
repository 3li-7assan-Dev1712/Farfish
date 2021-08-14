package com.example.friendlychat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private static final String TAG = MessagesAdapter.class.getSimpleName();
    private Context mContext;
    private List<Message> messages;

    private static final int USE_SENDER_BACKGROUND = 1;
    private static final int USE_SENDER_BACKGROUND_IMG = 3;
    private static final int USE_LOCAL_BACKGROUND = 0;
    private static final int USE_LOCAL_BACKGROUND_IMG = 2;


    public MessagesAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        String userName = messages.get(position).getName();
        String photoUrl = messages.get(position).getPhotoUrl();
        if (messages == null){
            throw new NullPointerException("From getItemViewType message is null");
        }
        if (userName == null || photoUrl == null) {
            return USE_SENDER_BACKGROUND;
        }

        if (useCurrentMessageBackground(userName) && !photoUrl.equals("")){
            return USE_LOCAL_BACKGROUND_IMG;
        }else if (useCurrentMessageBackground(userName) && photoUrl.equals("")){
            return USE_LOCAL_BACKGROUND;
        }else if (!useCurrentMessageBackground(userName) && photoUrl.equals("")){
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


    public boolean useCurrentMessageBackground(String senderName) {
        String userName = MessagesPreference.getUserName(mContext);
        return userName.equals(senderName);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        if (messages != null && messages.size() != 0){

            Message message = messages.get(position);
            String name =message.getName();
            String messageText = message.getText();
            String photoUrl = message.getPhotoUrl();
            if (photoUrl != null && !photoUrl.equals("")){
                Log.d(TAG, "photoUrl is: "+photoUrl);
                Picasso.get().load(photoUrl).placeholder(R.drawable.ic_baseline_emoji_emotions_24).into(holder.imageView);
            }else {
                holder.messageTextView.setText(messageText);
            }/*
            long messageTime = message.getTimestamp();
            Date date = new Date(messageTime);*/
            if (name == null)
                holder.authorName.setText("null");
            else
                holder.authorName.setText(name);
            long dateFromServer = message.getTimestamp();
            long currentTime = System.currentTimeMillis();
            if (dateFromServer == currentTime)
                Log.d(TAG, "There are the same");
            SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String readableDate = d.format(dateFromServer);
            Log.d(TAG, "readable date in Adapter is : " + readableDate);
            holder.timeMessageTextView.setText(readableDate);
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
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            imageView = itemView.findViewById(R.id.photoImageView);
            authorName = itemView.findViewById(R.id.nameTextView);
            timeMessageTextView = itemView.findViewById(R.id.timeMessage);

        }

    }

}
