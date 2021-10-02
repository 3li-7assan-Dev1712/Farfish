package com.example.friendlychat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
import com.example.friendlychat.databinding.ContactViewHolderBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    private static final String TAG = StatusAdapter.class.getSimpleName();
    private Context mContext;
    private List<List<Status>> statusLists;


    public interface OnStatusClicked {
        void onStatusClicked(int position);
    }

    public static OnStatusClicked onStatusClicked;

    public StatusAdapter(Context mContext, List<List<Status>> statusLists, OnStatusClicked pOnStatusClicked) {
        this.mContext = mContext;
        this.statusLists = statusLists;
        onStatusClicked = pOnStatusClicked;
    }


    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactViewHolderBinding contactViewHolderBinding =
                ContactViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StatusViewHolder(contactViewHolderBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        if (statusLists != null) {
            List<Status> statusList = statusLists.get(position);
            if (statusList != null) {
                if (statusList.size() > 0) {
                    Status lastStatus = statusList.get(statusList.size() - 1);
                    ((StatusViewHolder) holder).bind(lastStatus, statusList);
                }
            } else {
                Toast.makeText(mContext, "child statues are null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onBindViewHolder:child status are null");
            }
        } else {
            Toast.makeText(mContext, "status lists are null", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onBindViewHolder: no data status list is null ");
        }
    }

    @Override
    public int getItemCount() {
        if (statusLists == null) {
            Log.d(TAG, "getItemCount: you list is null");
            return 0;// I love java syntax *_*
        } else {
            Log.d(TAG, "getItemCount: list size is " + statusLists.size());
            return statusLists.size();
        }
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ContactViewHolderBinding binding;

        public StatusViewHolder(@NonNull ContactViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onStatusClicked.onStatusClicked(getBindingAdapterPosition());
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
                        binding.profileImage.setImageResource(R.drawable.place_holder);
                        binding.progressImageIndicator.setVisibility(View.GONE);
                        Log.d(TAG, "onError: Error loading status image");
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
