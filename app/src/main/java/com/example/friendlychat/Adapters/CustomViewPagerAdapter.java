package com.example.friendlychat.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.friendlychat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.callback.StoryCallbacks;
import omari.hamza.storyview.model.MyStory;
import omari.hamza.storyview.utils.ViewPagerAdapter;

public class CustomViewPagerAdapter extends ViewPagerAdapter {
    private Context context;
    private ArrayList<MyStory> images;
    private StoryCallbacks storyCallbacks;

    private  boolean storiesStarted = false;
    public CustomViewPagerAdapter(ArrayList<MyStory> images, Context context, StoryCallbacks storyCallbacks) {

        super(images, context, storyCallbacks);
        this.context = context;
        this.images = images;
        this.storyCallbacks = storyCallbacks;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {

        LayoutInflater inflater = LayoutInflater.from(context);

        MyStory currentStory = images.get(position);

        final View view = inflater.inflate(R.layout.custom_layout_story_item, collection, false);

        final ImageView mImageView = view.findViewById(R.id.mImageView);
        final TextView mTextView = view.findViewById(R.id.mTextViewStatus); /* will be used soon */

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
                      /*  PaletteExtraction pe = new PaletteExtraction(view.findViewById(R.id.relativeLayout),
                                ((BitmapDrawable) resource).getBitmap());

                        pe.execute();*/

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
        }else{
            mImageView.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText("Test to check the text status functionality");
            view.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }
        collection.addView(view);

        return view;
    }
}
