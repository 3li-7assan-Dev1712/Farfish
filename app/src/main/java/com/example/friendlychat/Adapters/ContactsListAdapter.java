package com.example.friendlychat.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.User;
import com.example.friendlychat.databinding.ContactViewHolderBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactsListAdapter extends ListAdapter<User, ContactsListAdapter.ContactsViewHolder> {


    private List<User> mUsers;

    public interface OnChatClicked {
        void onChatClicked(int position);
    }

    private static OnChatClicked onChatClicked;

    public ContactsListAdapter(List<User> users, OnChatClicked pOnChatClicked) {
        super(Diff);
        this.mUsers = users;
        onChatClicked = pOnChatClicked;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactViewHolderBinding binding =
                ContactViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.bind(user);
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {
        private ContactViewHolderBinding binding;

        public ContactsViewHolder(ContactViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(listener -> onChatClicked.onChatClicked(getBindingAdapterPosition()));
        }

        public void bind(User user) {
            Picasso.get().load(user.getPhotoUrl()).into(binding.profileImage);
            binding.personName.setText(user.getUserName());
            binding.lastMessage.setText(user.getStatus());
        }
    }

    public static final DiffUtil.ItemCallback<User> Diff = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };
}
