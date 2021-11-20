package com.example.farfish.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.R;
import com.example.farfish.databinding.ContactViewHolderBinding;
import com.example.farfish.databinding.LocalMessageViewHolderBinding;
import com.example.farfish.databinding.MessageViewHolderBinding;
import com.example.farfish.databinding.ReceiveImageViewHolderBinding;
import com.example.farfish.databinding.SendImageViewHolderBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This adapter is used in both UserChatsFragment and ChatsFragment for providing
 * the data to the RecyclerView.
 */
public class MessagesListAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {


    private static final DiffUtil.ItemCallback<Message> Diff = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.equals(newItem);
        }
    };
    private final String TAG = MessagesListAdapter.class.getSimpleName();
    private static final int USE_RECEIVE_BACKGROUND = 0;
    private static final int USE_SEND_BACKGROUND = 1;
    private static final int USE_SEND_BACKGROUND_IMG = 2;
    private static final int USE_RECEIVE_BACKGROUND_IMG = 3;
    private static final int USE_PARENT_VIEW_HOLDER = 4;
    private AsyncListDiffer<Message> mDiffer;
    MessagesListAdapter.MessageClick mMessageInterface;
    ChatClick mChatClick;
    private List<Message> mMessages;
    private Context mContext;
    // this boolean is for UserChatsFragment's ViewHolders
    private boolean mIsGeneral;

   /* public MessagesListAdapter(List<Message> messages, Context context, MessageClick messageClick, boolean isGeneral) {
        super(MessagesListAdapter.Diff);
        this.mMessages = messages;
        this.mContext = context;
        mMessageInterface = messageClick;
        this.mIsGeneral = isGeneral;
    }

    public MessagesListAdapter(List<Message> messages, Context context, ChatClick chatClick, boolean isGeneral) {
        super(MessagesListAdapter.Diff);
        this.mMessages = messages;
        this.mContext = context;
        mChatClick = chatClick;
        this.mIsGeneral = isGeneral;
    }*/

    public MessagesListAdapter(Context mContext) {
        super(Diff);
        mDiffer= new AsyncListDiffer<>(this, Diff);
        this.mContext = mContext;
    }

    private static String getReadableDate(long timestamp) {
        SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return d.format(timestamp);
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsGeneral)
            return USE_PARENT_VIEW_HOLDER;
        String senderId = mDiffer.getCurrentList().get(position).getSenderId();
        String photoUrl = mDiffer.getCurrentList().get(position).getPhotoUrl();
        if (useCurrentMessageBackground(senderId) && !photoUrl.equals("")) {
            return USE_SEND_BACKGROUND_IMG;
        } else if (useCurrentMessageBackground(senderId) && photoUrl.equals("")) {
            return USE_SEND_BACKGROUND;
        } else if (!useCurrentMessageBackground(senderId) && photoUrl.equals("")) {
            return USE_RECEIVE_BACKGROUND;
        } else {
            return USE_RECEIVE_BACKGROUND_IMG;
        }
    }

    public boolean useCurrentMessageBackground(String senderId) {
        String currentUserId = MessagesPreference.getUserId(mContext);
        return currentUserId.equals(senderId);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case USE_PARENT_VIEW_HOLDER:
                ContactViewHolderBinding contactViewHolderBinding
                        = ContactViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new UserChatsViewHolder(contactViewHolderBinding);
            case USE_SEND_BACKGROUND:
                LocalMessageViewHolderBinding localMessageViewHolderBinding
                        = LocalMessageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new LocalMessageViewHolder(localMessageViewHolderBinding);
            case USE_RECEIVE_BACKGROUND:
                MessageViewHolderBinding messageViewHolderBinding =
                        MessageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ReceivedMessageViewHolder(messageViewHolderBinding);
            case USE_SEND_BACKGROUND_IMG:
                SendImageViewHolderBinding sendImageViewHolderBinding
                        = SendImageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new LocalImageMessageViewHolder(sendImageViewHolderBinding);
            default:
                ReceiveImageViewHolderBinding receiveImageViewHolderBinding =
                        ReceiveImageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ReceivedImageMessageViewHolder(receiveImageViewHolderBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mDiffer.getCurrentList().get(position);
        switch (holder.getItemViewType()) {
            case USE_PARENT_VIEW_HOLDER:
                UserChatsViewHolder chatsViewHolder = (UserChatsViewHolder) holder;
                chatsViewHolder.bind(message);
                break;
            case USE_SEND_BACKGROUND:
                LocalMessageViewHolder locaMessage = (LocalMessageViewHolder) holder;
                locaMessage.bind(message);
                break;
            case USE_RECEIVE_BACKGROUND:
                ReceivedMessageViewHolder receiveMessage = (ReceivedMessageViewHolder) holder;
                receiveMessage.bind(message);
                break;
            case USE_SEND_BACKGROUND_IMG:
                LocalImageMessageViewHolder localImageMessageViewHolder = (LocalImageMessageViewHolder) holder;
                localImageMessageViewHolder.bind(message);
                break;
            case USE_RECEIVE_BACKGROUND_IMG:
                ReceivedImageMessageViewHolder receivedIMageMessage = (ReceivedImageMessageViewHolder) holder;
                receivedIMageMessage.bind(message);
                break;
        }
    }

    public void setIsGeneral(boolean mIsGeneral) {
        this.mIsGeneral = mIsGeneral;
    }

    public void setChatClick(ChatClick chatClick) {
        this.mChatClick = chatClick;
    }

    public void setMessageInterface(MessageClick mMessageInterface) {
        this.mMessageInterface = mMessageInterface;
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @Override
    public void submitList(@Nullable List<Message> list) {
        assert list != null;
        mDiffer.submitList(new ArrayList<>(list));
        /*notifyDataSetChanged();*/
    }

    /* interface for listening to touching*/
    public interface MessageClick {
        void onMessageClick(View view, int position);
    }

    public interface ChatClick {
        void onChatClick(int position);
    }

    static class LocalMessageViewHolder extends RecyclerView.ViewHolder {

        private LocalMessageViewHolderBinding binding;

        public LocalMessageViewHolder(@NonNull LocalMessageViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(Message message) {
            binding.nameTextView.setText(message.getSenderName());
            binding.messageTextView.setText(message.getText());
            binding.timeMessage.setText(getReadableDate(message.getTimestamp()));
            if (message.getIsRead())
                binding.isReadIconIndicator.setImageResource(R.drawable.ic_done_all_24);
            else
                binding.isReadIconIndicator.setImageResource(R.drawable.ic_done_all_black);
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private MessageViewHolderBinding binding;

        public ReceivedMessageViewHolder(@NonNull MessageViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            binding.nameTextView.setText(message.getSenderName());
            binding.messageTextView.setText(message.getText());
            binding.timeMessage.setText(getReadableDate(message.getTimestamp()));
        }
    }

    class LocalImageMessageViewHolder extends RecyclerView.ViewHolder {


        private SendImageViewHolderBinding binding;

        public LocalImageMessageViewHolder(@NonNull SendImageViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.photoImageView.setOnClickListener(view -> {
                mMessageInterface.onMessageClick(view, getBindingAdapterPosition());
            });
        }

        public void bind(Message message) {
            ViewCompat.setTransitionName(binding.photoImageView, String.valueOf(message.getTimestamp()));
            binding.nameTextView.setText(message.getSenderName());
            Picasso.get().load(message.getPhotoUrl()).into(binding.photoImageView, new Callback() {
                @Override
                public void onSuccess() {
                    binding.sendImageProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    binding.sendImageProgressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onError: " + e.getMessage());
                }
            });
            binding.timeMessage.setText(getReadableDate(message.getTimestamp()));
            if (message.getIsRead())
                binding.isReadIconIndicator.setImageResource(R.drawable.ic_done_all_24);
            else
                binding.isReadIconIndicator.setImageResource(R.drawable.ic_done_all_black);
        }

    }

    class ReceivedImageMessageViewHolder extends RecyclerView.ViewHolder {
        private ReceiveImageViewHolderBinding binding;

        public ReceivedImageMessageViewHolder(@NonNull ReceiveImageViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.photoImageView.setOnClickListener(view -> {
                mMessageInterface.onMessageClick(view, getBindingAdapterPosition());
            });
        }

        public void bind(Message message) {
            ViewCompat.setTransitionName(binding.photoImageView, String.valueOf(message.getTimestamp()));
            binding.nameTextView.setText(message.getSenderName());
            Picasso.get().load(message.getPhotoUrl()).into(binding.photoImageView, new Callback() {
                @Override
                public void onSuccess() {
                    binding.receiveImageProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    binding.receiveImageProgressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onError: " + e.getMessage());

                }
            });
            binding.timeMessage.setText(getReadableDate(message.getTimestamp()));
        }

    }

    class UserChatsViewHolder extends RecyclerView.ViewHolder {
        private ContactViewHolderBinding binding;

        public UserChatsViewHolder(ContactViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(listener -> {
                mChatClick.onChatClick(getBindingAdapterPosition());
            });

        }

        public void bind(Message message) {
            Picasso.get().load(message.getTargetPhotoUrl()).placeholder(R.drawable.time_background).into(binding.profileImage, new Callback() {
                @Override
                public void onSuccess() {
                    binding.progressImageIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    binding.progressImageIndicator.setVisibility(View.GONE);
                }
            });
            binding.personName.setText(message.getTargetName());
            if (!message.getText().equals(""))
                binding.lastMessage.setText(message.getText());
            else {
                binding.lastMessage.setText(R.string.new_photo_view_holder);
                /*at the first of the text set this drawable to indicate of new photo*/
                binding.lastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_baseline_photo_camera_24,
                        0,
                        0,
                        0
                );
            }
            binding.lastMessageTime.setText(getReadableDate(message.getTimestamp()));
            int newMessageNumber = message.getNewMessagesCount();
            if (newMessageNumber > 0) {
                binding.newMessageCountTextViewContainer.setVisibility(View.VISIBLE);
                binding.newMessageCountTextView.setText(String.valueOf(newMessageNumber));
                binding.lastMessage.setTextColor(binding.getRoot().getContext().getResources().getColor(R.color.colorPrimary));
                binding.lastMessageTime.setTextColor(binding.getRoot().getContext().getResources().getColor(R.color.colorPrimary));
            }
        }

    }

}
