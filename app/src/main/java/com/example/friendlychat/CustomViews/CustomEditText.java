package com.example.friendlychat.CustomViews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;

import com.example.friendlychat.R;

public class CustomEditText extends AppCompatEditText {
    private Drawable mEmoji;
    private Drawable mClearButtonText;  /*These are the drawables that will be visible in the EditText*/
    public CustomEditText(@NonNull Context context) {

        super(context);
        init();
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mClearButtonText = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_baseline_close_24_50_gray, null);
        /*when text changes show or hide the clear button*/
        // when pressing to the clear button it should clear all the text written in the EditText.
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showClearButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((getCompoundDrawablesRelative()[2] != null)) {
                    float clearButtonStart; // Used for LTR languages
                    float clearButtonEnd;  // Used for RTL languages
                    boolean isClearButtonClicked = false;
                    if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                        // If RTL, get the end of the button on the left side.
                        clearButtonEnd = mClearButtonText
                                .getIntrinsicWidth() + getPaddingStart();
                        // If the touch occurred before the end of the button,
                        // set isClearButtonClicked to true.
                        if (event.getX() < clearButtonEnd) {
                            isClearButtonClicked = true;
                        }
                    } else {
                        // Layout is LTR.
                        // Get the start of the button on the right side.
                        clearButtonStart = (getWidth() - getPaddingEnd()
                                - mClearButtonText.getIntrinsicWidth());
                        // If the touch occurred after the start of the button,
                        // set isClearButtonClicked to true.
                        if (event.getX() > clearButtonStart) {
                            isClearButtonClicked = true;
                        }
                    }
                    if (isClearButtonClicked) {
                        // Check for ACTION_DOWN (always occurs before ACTION_UP).
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            // Switch to the black version of clear button.
                            mClearButtonText =
                                    ResourcesCompat.getDrawable(getResources(),
                                            R.drawable.ic_baseline_close_24, null);
                            showClearButton();
                        }
                        // Check for ACTION_UP.
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            // Switch to the opaque version of clear button.
                            mClearButtonText =
                                    ResourcesCompat.getDrawable(getResources(),
                                            R.drawable.ic_baseline_close_24_50_gray, null);
                            // Clear the text and hide the clear button.
                            getText().clear();
                            hideClearButton();
                            return true;
                        }
                    } else {
                        return false;
                    }

                    // TODO: Check for actions if the button is tapped.
                }
                return false;

            }
        });

    }

    /**
     *  method for showing the button
     */
    private void showClearButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds
                (null,                      // Start of text.
                        null,               // Above text.
                        mClearButtonText,  // End of text.
                        null);              // Below text.
    }

    /**
     * method for hiding the button
     */
    private void hideClearButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds
                (null,             // Start of text.
                        null,      // Above text.
                        null,      // End of text.
                        null);     // Below text.
    }

}
