package com.example.friendlychat.Adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>{
    private Context mContext;
    private List<User> users;
    private List<Message> messages;


    public interface OnChatClicked {
        void onChatClicked(int position);
    }
    private static OnChatClicked onChatClicked;
    public ContactsAdapter(Context mContext, List<User> users, OnChatClicked onChatClicked) {
        this.mContext = mContext;
        this.users = users;
        ContactsAdapter.onChatClicked = onChatClicked;
    }
    public ContactsAdapter(Context mContext, List<Message> messages, OnChatClicked onChatClicked, List<User> users) {
        this.mContext = mContext;
        this.messages = messages;
        this.users = users;
        ContactsAdapter.onChatClicked = onChatClicked;
    }




    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.contact_view_holder, parent, false);
        return new ContactsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        if (users != null){
            User user = users.get(position);
            holder.userName.setText(user.getUserName());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){
                Picasso.get().load(users.get(position).getPhotoUrl()).into(holder.userPhoto);
            }else {
                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.fui_ic_anonymous_white_24dp).into(holder.userPhoto);
            }
            String userStatus = users.get(position).getStatus();
            holder.lastMessageTextView.setText(userStatus);

        }else if (messages != null){
            Message message = messages.get(position);
            String targetUserName = message.getTargetName();
            String targetUserPhotoUrl = message.getTargetPhotoUrl();
            holder.userName.setText(targetUserName);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){
                Picasso.get().load(targetUserPhotoUrl).into(holder.userPhoto);
            }else {
                Picasso.get().load(targetUserPhotoUrl).placeholder(R.drawable.fui_ic_anonymous_white_24dp).into(holder.userPhoto);
            }

            String messageText = message.getText();
            /* if the message is equals to ("") then there's a new photo */
            if (!messageText.equals(""))
                holder.lastMessageTextView.setText(messageText);
            else {
                holder.lastMessageTextView.setText(R.string.new_photo_view_holder);
                /*at the first of the text set this drawable to indicate of new photo*/
                holder.lastMessageTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_baseline_photo_camera_24,
                        0,
                        0,
                        0
                );
            }
            long messageTime = message.getTimestamp();
            SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String readableDate = d.format(messageTime);
            holder.lastMessageTimeTextView.setText(readableDate);
        }

    }

    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else if (users != null)
            return users.size();
        else {
            Log.d("TAG", "check the getItemCount int the ContactsAdapter");
            return 0;
        }
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView userName;
        private TextView lastMessageTextView;
        private TextView lastMessageTimeTextView;
        private ImageView userPhoto;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.personName);
            userPhoto = itemView.findViewById(R.id.profileImage);
            lastMessageTextView = itemView.findViewById(R.id.lastMessage);
            lastMessageTimeTextView = itemView.findViewById(R.id.lastMessageTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onChatClicked.onChatClicked(getBindingAdapterPosition());
        }
    }

    public void setUsers(List<User> users) {
        if (users != null) {
            this.users = users;
            notifyDataSetChanged();
        }
    }
}
