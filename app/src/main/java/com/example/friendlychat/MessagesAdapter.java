package com.example.friendlychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> messages;

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

        /*will be done in the next commit*/
    }

    @Override
    public int getItemCount() {
        /*will be done in the next commit*/
        return 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder{
        /*will be done in the next commit*/
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
