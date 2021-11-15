package com.example.farfish.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.farfish.CustomViews.CustomStory;
import com.example.farfish.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.callback.StoryCallbacks;
import omari.hamza.storyview.utils.ViewPagerAdapter;

/**
 * This adapter is used in the StatusFragment to display a set of statuses after tabbing
 * on an item in the RecyclerView.
 */
public class CustomViewPagerAdapter extends ViewPagerAdapter {
    private Context context;
    private ArrayList<CustomStory> stories;
    private StoryCallbacks storyCallbacks;

    private boolean storiesStarted = false;

    public CustomViewPagerAdapter(ArrayList<CustomStory> stories, Context context, StoryCallbacks storyCallbacks) {

        super(null, context, storyCallbacks);
        this.context = context;
        this.stories = stories;
        this.storyCallbacks = storyCallbacks;
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    public void setStories(ArrayList<CustomStory> customStories) {
        this.stories = customStories;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {

        LayoutInflater inflater = LayoutInflater.from(context);

        CustomStory currentStory = stories.get(position);

        final View view = inflater.inflate(R.layout.custom_layout_story_item, collection, false);

        final ImageView mImageView = view.findViewById(R.id.mImageView);
        final TextView mTextView = view.findViewById(R.id.mTextViewStatus);

        if (!TextUtils.isEmpty(currentStory.getDescription())) {
            TextView textView = view.findViewById(R.id.descriptionTextView);
            textView.setVisibility(View.VISIBLE);
            textView.setText(currentStory.getDescription());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    storyCallbacks.onDescriptionClickListener(position);
                }
            });
        }

        String imageUrl = currentStory.getUrl();
        if (!imageUrl.equals("")) {

            Picasso.get().load(currentStory.getUrl())
                    .into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (!storiesStarted) {
                                storiesStarted = true;
                                storyCallbacks.startStories();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            storyCallbacks.nextStory();
                        }
                    });
        } else {
            mImageView.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(currentStory.getStatusText());
            view.setBackgroundColor(context.getResources().getColor(R.color.secondaryDarkColor));
            if (!storiesStarted) {
                storiesStarted = true;
                storyCallbacks.startStories();
            }
        }
        collection.addView(view);

        return view;
    }
}
