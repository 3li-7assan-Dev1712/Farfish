package com.example.friendlychat.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Message;
import com.example.friendlychat.MessagesPreference;
import com.example.friendlychat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> messages;

    private static final int USE_NORMAL_BACKGROUND = 0;
    private static final int USE_CURRENT_MESSAGE_BACKGROUND = 1;
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


    public boolean useCurrentMessageBackground(String senderName) {
        String userName = MessagesPreference.getUserName(mContext);
        return userName.equals(senderName);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        if (messages != null && messages.size() != 0){

            LinearLayout.LayoutParams paramsMsg = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Message message = messages.get(position);
            String name =message.getName();
            String messageText = message.getText();
            String photoUrl = message.getPhotoUrl();
            if (photoUrl != null && !photoUrl.equals("")){
                holder.itemView.setPadding(0,0,0,0);/*to make full bleed image*/
                holder.messageTextView.setVisibility(View.GONE);
                Picasso.get().load(photoUrl).into(holder.imageView);
            }else {
                holder.imageView.setVisibility(View.GONE);
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.messageTextView.setText(messageText);
            }
            holder.authorName.setText(name);
            if (useCurrentMessageBackground(name)){
                holder.messageLayout.setBackground(mContext.getResources().getDrawable(R.drawable.current_message_background));
                /*paramsMsg.gravity = Gravity.END;*/
                holder.layout.setGravity(Gravity.END);
            }
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
        private LinearLayout layout;
        private LinearLayout messageLayout;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            imageView = itemView.findViewById(R.id.photoImageView);
            authorName = itemView.findViewById(R.id.nameTextView);
            layout = itemView.findViewById(R.id.bodyView);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }

    }
}
