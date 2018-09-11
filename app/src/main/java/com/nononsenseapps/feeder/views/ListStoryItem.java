package com.nononsenseapps.feeder.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.nononsenseapps.feeder.R;

/**
 * A list item suitable for devices such as phones or where more narrow lists are displayed.
 */
public class ListStoryItem extends FrameLayout {

    private final ImageView mStoryImage;
    private final TextView mStoryDate;
    private final TextView mStoryAuthor;
    private final TextView mStorySnippet;
    private final int imgWidth;

    public ListStoryItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListStoryItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        imgWidth = Math.round(context.getResources().getDimension(R.dimen.item_img_def_width));

        LayoutInflater.from(context).inflate(R.layout.list_story_item_layout, this, true);

        mStoryImage = findViewById(R.id.story_image);
        mStoryDate = findViewById(R.id.story_date);
        mStoryAuthor = findViewById(R.id.story_author);
        mStorySnippet = findViewById(R.id.story_snippet);
    }

    private void layoutView(View view, int left, int top, int width, int height) {
        MarginLayoutParams margins = (MarginLayoutParams) view.getLayoutParams();
        final int leftWithMargins = left + margins.leftMargin;
        final int topWithMargins = top + margins.topMargin;
        view.layout(leftWithMargins, topWithMargins,
                leftWithMargins + width, topWithMargins + height);
    }

    private int getMeasuredWidthWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
    }

    private int getMeasuredHeightWithMargins(View child) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }

    /**
     * {@inheritDoc}
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        final int dateWidth = getMeasuredWidthWithMargins(mStoryDate);
        final int dateHeight = getMeasuredHeightWithMargins(mStoryDate);
        final int snippetHeight = getMeasuredHeightWithMargins(mStorySnippet);

        int textRight = getRight();
        // End padding depends on image visibility
        if (mStoryImage.getVisibility() == View.GONE) {
            textRight -= paddingRight;
        } else {
            textRight -= imgWidth;
        }
        final int dateLeft = textRight - dateWidth;


        int currentTop = paddingTop;

        layoutView(mStoryDate, dateLeft, currentTop, dateWidth, dateHeight);
        layoutView(mStoryAuthor, paddingLeft, currentTop, dateLeft - paddingLeft, dateHeight);

        currentTop += dateHeight;

        layoutView(mStorySnippet, paddingLeft, currentTop, textRight - paddingLeft, snippetHeight + paddingBottom);

        currentTop += snippetHeight + paddingBottom;

        // Image last
        if (mStoryImage.getVisibility() != View.GONE) {
          layoutView(mStoryImage, getRight() - imgWidth, 0, imgWidth, currentTop);//getMeasuredHeightWithMargins(mStoryImage));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthUsed = getPaddingLeft();
        int heightUsed = 0;

        // TODO could setPaddingRight to 0
        if (mStoryImage.getVisibility() != View.GONE) {
            widthUsed += imgWidth;
        } else {
            widthUsed += getPaddingRight();
        }

        measureChildWithMargins(mStorySnippet,
                widthMeasureSpec, widthUsed,
                heightMeasureSpec, heightUsed);
        heightUsed += getMeasuredHeightWithMargins(mStorySnippet);

        measureChildWithMargins(mStoryDate,
                widthMeasureSpec, widthUsed,
                heightMeasureSpec, heightUsed);
        widthUsed += getMeasuredWidthWithMargins(mStoryDate);
        // They are on the same height
        measureChildWithMargins(mStoryAuthor,
                widthMeasureSpec, widthUsed,
                heightMeasureSpec, heightUsed);

        // Now update height
        heightUsed += getMeasuredHeightWithMargins(mStoryDate);

        // Respect minimum size
        int heightSize = heightUsed;
        if (heightSize < getMinimumHeight()) {
            heightSize = getMinimumHeight();
        }

        if (mStoryImage.getVisibility() != View.GONE) {
            measureChildWithMargins(mStoryImage,
                    widthMeasureSpec, getPaddingLeft(),
                    heightMeasureSpec, 0);
        }

        setMeasuredDimension(widthSize, heightSize);
    }
}
