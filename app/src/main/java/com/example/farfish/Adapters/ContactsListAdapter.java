package com.example.farfish.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.R;
import com.example.farfish.databinding.ContactViewHolderBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * this adapter is used in both UsersFragment and StatusFragment to provide
 * the data for the RecyclerView.
 */
public class ContactsListAdapter extends ListAdapter<User, ContactsListAdapter.ContactsViewHolder> {


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
    public final DiffUtil.ItemCallback<List<Status>> StatusDiff = new DiffUtil.ItemCallback<List<Status>>() {
        @Override
        public boolean areItemsTheSame(@NonNull List<Status> oldItem, @NonNull List<Status> newItem) {
            if (oldItem.size() == 0 || newItem.size() == 0)
                return false;
            return oldItem.get(0).getTimestamp() == newItem.get(0).getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(@NonNull List<Status> oldItem, @NonNull List<Status> newItem) {
            return oldItem.size() == newItem.size();
        }
    };
    private final String TAG = ContactsListAdapter.class.getSimpleName();
    // for status destination
    private final AsyncListDiffer<List<Status>> mStatusDiffer = new AsyncListDiffer<>(this, StatusDiff);
    // significant attribute to hunt the difference between two lists on a background thread
    private final AsyncListDiffer<User> mDiffer = new AsyncListDiffer<>(this, Diff);
    private OnChatClicked onChatClicked;
    private boolean forStatus;


    public ContactsListAdapter(OnChatClicked pOnChatClicked, boolean forStatus) {
        super(Diff);
        onChatClicked = pOnChatClicked;
        this.forStatus = forStatus;
    }

    public ContactsListAdapter() {
        super(Diff);
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactViewHolderBinding binding =
                ContactViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactsViewHolder(binding);
    }

    public void setOnChatClicked(OnChatClicked onChatClicked) {
        this.onChatClicked = onChatClicked;
    }

    public void setForStatus(boolean forStatus) {
        this.forStatus = forStatus;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        if (forStatus) {
            List<Status> statusList = mStatusDiffer.getCurrentList().get(position);
            if (statusList != null) {
                if (statusList.size() > 0) {
                    Status lastStatus = statusList.get(statusList.size() - 1);
                    holder.bind(lastStatus, statusList);
                }
            } else {
                Log.d(TAG, "onBindViewHolder:child status are null");
            }
        } else {
            User user = mDiffer.getCurrentList().get(position);
            holder.bind(user);
        }
    }

    @Override
    public int getItemCount() {

        return (forStatus)? mStatusDiffer.getCurrentList().size(): mDiffer.getCurrentList().size();
    }

    public void customSubmitUserList( @Nullable List<User> list) {
        mDiffer.submitList(list);
    }

    public void customSubmitStatusList(@Nullable List<List<Status>> lists) {
        if (lists != null) {
            mStatusDiffer.submitList(new ArrayList<>(lists));
        }
    }

    public interface OnChatClicked {
        void onChatClicked(int position);
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(Status lastStatus, List<Status> statusList) {
            String uploaderName = lastStatus.getUploaderName();
            String statusImage = lastStatus.getStatusImage();
            String statusText = lastStatus.getStatusText();
            binding.circleStatusView.setPortionsCount(statusList.size());
            binding.circleStatusView.setVisibility(View.VISIBLE);
            long statusTime = lastStatus.getTimestamp();
            SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String readableDate = d.format(statusTime);
            binding.personName.setText(uploaderName);
            binding.lastMessageTime.setText(readableDate);
            if (!statusImage.equals("")) {
                binding.profileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(statusImage).into(binding.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        binding.progressImageIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.profileImage.setImageResource(R.drawable.time_background);
                        binding.progressImageIndicator.setVisibility(View.GONE);
                    }
                });
            } else {
                /* text status comes to the party*/
                binding.textStatusAsItem.setText(statusText);
                binding.textStatusAsItem.setVisibility(View.VISIBLE);
                binding.progressImageIndicator.setVisibility(View.GONE);
                binding.circleStatusView.setPortionsColor(binding.getRoot().getContext().getResources().getColor(R.color.primaryLightColor));
            }
        }
    }

}
