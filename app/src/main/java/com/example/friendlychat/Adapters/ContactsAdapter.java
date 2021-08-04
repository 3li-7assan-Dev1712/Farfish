package com.example.friendlychat.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.R;
import com.example.friendlychat.User;
import com.google.firebase.installations.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>{
    private Context mContext;
    private List<User> users;

    public interface OnChatClicked {
        void onChatClicked(int position);
    }
    private static OnChatClicked onChatClicked;
    public ContactsAdapter(Context mContext, List<User> users) {
        this.mContext = mContext;
        this.users = users;
    }

    public ContactsAdapter(Context mContext, List<User> users, OnChatClicked onChatClicked) {
        this.mContext = mContext;
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
            holder.userName.setText(users.get(position).getUserName());
            holder.lastMessageTime.setText("2:04PM");
            holder.lastMessage.setText("OK, I will do that If Allah wills");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){
                Picasso.get().load(users.get(position).getPhotoUrl()).into(holder.userPhoto);
            }else {
                Picasso.get().load(users.get(position).getPhotoUrl()).placeholder(R.drawable.fui_ic_anonymous_white_24dp).into(holder.userPhoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (users == null)? 0: users.size();
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView userName;
        private TextView lastMessage;
        private TextView lastMessageTime;
        private ImageView userPhoto;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.personName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTime= itemView.findViewById(R.id.lastMessageTime);
            userPhoto = itemView.findViewById(R.id.profileImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onChatClicked.onChatClicked(getBindingAdapterPosition());
        }
    }
}
