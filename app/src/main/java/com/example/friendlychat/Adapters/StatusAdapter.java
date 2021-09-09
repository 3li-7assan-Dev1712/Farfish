package com.example.friendlychat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.contact_view_holder, parent, false);
        return new StatusViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        if (statusLists != null){
            List<Status> statusList = statusLists.get(position);
            if (statusList != null) {
                if (statusList.size() > 0) {
                    Status lastStatus = statusList.get(statusList.size() - 1);
                    String uploaderName = lastStatus.getUploaderName();
                    String statusImage = lastStatus.getStatusImage();
                    String statusText = lastStatus.getStatusText(); // will be used soon
                    long statusTime = lastStatus.getTimestamp();
                    SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    String readableDate = d.format(statusTime);
                    holder.uploaderName.setText(uploaderName);
                    holder.uploadDate.setText(readableDate);
                    if (!statusImage.equals(""))
                        Picasso.get().load(statusImage).placeholder(R.drawable.group_icon).into(holder.statusImage);
                }
            }else{
                Toast.makeText(mContext, "child statues are null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onBindViewHolder:child status are null");
            }
        }
        else {
            Toast.makeText(mContext, "status lists are null", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onBindViewHolder: no data status list is null ");
        }
    }

    @Override
    public int getItemCount() {
        return (statusLists == null) ? 0: statusLists.size(); // I love java syntax *_*
    }
    static class StatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView uploaderName;
        private TextView uploadDate;
        private ImageView statusImage;
        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            uploaderName = itemView.findViewById(R.id.personName);
            statusImage = itemView.findViewById(R.id.profileImage);
            uploadDate = itemView.findViewById(R.id.lastMessage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onStatusClicked.onStatusClicked(getBindingAdapterPosition());
        }
    }
}