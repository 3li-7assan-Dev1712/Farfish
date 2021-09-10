package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class UserStatus extends Fragment {

    private  String mStatusImageUrl, mStatusText;

    public UserStatus(String mStatusImageUrl, String mStatusText) {
        this.mStatusImageUrl = mStatusImageUrl;
        this.mStatusText = mStatusText;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_status, container, false);
        ProgressBar statusProgressBar = view.findViewById(R.id.statusProgressBar);
        statusProgressBar.setVisibility(View.VISIBLE);
        ImageView statusImage = view.findViewById(R.id.imageViewUserStatus);
        TextView statusText = view.findViewById(R.id.textViewUserStatus);
        if (!mStatusImageUrl.equals("")){
            statusText.setVisibility(View.GONE);
            Picasso.get().load(mStatusImageUrl).into(statusImage, new Callback() {
                @Override
                public void onSuccess() {
                    // hide the progress bar after successfully loading image
                    statusProgressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    // if there's an error it will almost be caused by the lack of the internet
                    Toast.makeText(getActivity(), "Error loading the image check your internet connection", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            statusImage.setVisibility(View.GONE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(mStatusText);
            view.setBackgroundColor(requireActivity().getResources().getColor(R.color.colorAccent));
            statusProgressBar.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}
