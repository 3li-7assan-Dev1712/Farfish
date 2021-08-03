package com.example.friendlychat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.R;
import com.example.friendlychat.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>{
    private Context mContext;
    private List<User> users;

    public ContactsAdapter(Context mContext, List<User> users) {
        this.mContext = mContext;
        this.users = users;
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
            for (User user: users){
                holder.userName.setText(user.getUserName());
                holder.lastMessageTime.setText("2:04PM");
                holder.lastMessage.setText("OK, I will do that If Allah wills");
                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.fui_ic_anonymous_white_24dp).into(holder.userPhoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (users == null)? 0: users.size();
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder{
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
        }
    }
}
