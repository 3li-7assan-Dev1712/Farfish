package com.example.friendlychat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.User;
import com.example.friendlychat.databinding.ContactViewHolderBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactsListAdapter extends ListAdapter<User, ContactsListAdapter.ContactsViewHolder> {


    public interface OnChatClicked {
        void onChatClicked(int position);
    }

    private static OnChatClicked onChatClicked;

    public ContactsListAdapter(OnChatClicked pOnChatClicked) {
        super(Diff);
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
        User user = mDiffer.getCurrentList().get(position);
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
            Picasso.get().load(user.getPhotoUrl()).into(binding.profileImage, new Callback() {
                @Override
                public void onSuccess() {
                    binding.progressImageIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    binding.progressImageIndicator.setVisibility(View.GONE);
                }
            });
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

    // significant attribute to hunt the difference between two lists on a background thread
    private final AsyncListDiffer<User> mDiffer = new AsyncListDiffer<>(this, Diff);

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @Override
    public void submitList(@Nullable List<User> list) {
        mDiffer.submitList(list);
    }
}
