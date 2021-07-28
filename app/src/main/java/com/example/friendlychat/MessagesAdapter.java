package com.example.friendlychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> messages;

    public void setMessages(List<Message> messages) {

        this.messages = messages;
        notifyDataSetChanged();

    }

    public MessagesAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.message_view_holder, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        if (messages != null && messages.size() != 0){
            Message message = messages.get(position);
            String name =message.getName();
            String messageText = message.getText();
            String photoUrl = message.getPhotoUrl();

            if (photoUrl != null && !photoUrl.equals("")){
                holder.messageTextView.setVisibility(View.GONE);
                Picasso.get().load(photoUrl).into(holder.imageView);
            }else {
                holder.imageView.setVisibility(View.GONE);
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.messageTextView.setText(messageText);
            }
            holder.authorName.setText(name);
        }
        /*will be done in the next commit*/
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
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            imageView = itemView.findViewById(R.id.photoImageView);
            authorName = itemView.findViewById(R.id.nameTextView);
        }

    }
}
