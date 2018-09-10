package com.nononsenseapps.feeder.views;

/*
 * Copyright (C) 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;

/**
 * Layout which an {@link android.widget.EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 * <p/>
 * Extension:
 * Shows an optional error label, with live checking
 */
public final class FloatLabelLayout extends FrameLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 4f;

    private EditText mEditText;
    private TextView mLabel;
    private TextView mError;

    private Validator mValidator = null;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        final int sidePadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelSidePadding,
                dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));
        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);

        mLabel.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small));

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // Error label
        mError = new TextView(context);
        mError.setPadding(sidePadding, 0, sidePadding, 0);
        mError.setVisibility(INVISIBLE);

        mError.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatLabelLayout_floatErrorTextAppearance,
                        android.R.style.TextAppearance_Small));

        addView(mError, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        a.recycle();
    }

    @Override
    public final void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // If we already have an EditText, throw an exception
            if (mEditText != null) {
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            }

            // Update the layout params so that the EditText is on bottom, with enough
            // margin to show the labels
            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = Math.max((int) mLabel.getTextSize(), (int) mError.getTextSize());
            params = lp;

            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    public void setValidator(Validator validator) {
        mValidator = validator;
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    private void setEditText(EditText editText) {
        mEditText = editText;

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String errorText = null;
                if (mValidator != null) {
                    errorText = mValidator.afterTextChanged(s);
                }
                if (errorText == null || errorText.isEmpty()) {
                    if (mError.getVisibility() == View.VISIBLE) {
                        hideLabel(mError);
                    }
                    if (TextUtils.isEmpty(s)) {
                        // The text is empty, so hide the label if it is visible
                        if (mLabel.getVisibility() == View.VISIBLE) {
                            hideLabel(mLabel);
                        }
                    } else {
                        // The text is not empty, so show the label if it is not visible
                        if (mLabel.getVisibility() != View.VISIBLE) {
                            showLabel(mLabel);
                        }
                    }
                } else {
                    mError.setText(errorText);
                    if (mLabel.getVisibility() == View.VISIBLE) {
                        hideLabel(mLabel);
                    }
                    if (mError.getVisibility() != View.VISIBLE) {
                        showLabel(mError);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                mLabel.setActivated(focused);
            }
        });

        mLabel.setText(mEditText.getHint());
    }

    /**
     * @return the {@link android.widget.TextView} error label
     */
    public TextView getError() {
        return mError;
    }


    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */
    private void showLabel(View label) {
        label.setVisibility(View.VISIBLE);
        label.setAlpha(0f);
        label.setTranslationY(label.getHeight());
        label.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null).start();
    }

    /**
     * Hide the label using an animation
     */
    private void hideLabel(final View label) {
        label.setAlpha(1f);
        label.setTranslationY(0f);
        label.animate()
                .alpha(0f)
                .translationY(label.getHeight())
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        label.setVisibility(View.GONE);
                    }
                }).start();
    }

    /**
     * Display the specified error
     */
    public void showError(String error) {
        mError.setText(error);
        if (mLabel.getVisibility() == View.VISIBLE) {
            hideLabel(mLabel);
        }
        if (mError.getVisibility() != View.VISIBLE) {
            showLabel(mError);
        }
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }

    public interface Validator {
        public String afterTextChanged(Editable s);
    }
}
