package com.richluick.android.roomie.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.richluick.android.roomie.R;

public class ClickableImageView extends FrameLayout {

    private ImageView image;
    private ImageView cover;

    public ClickableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.clickable_image_view, this);

        image = (ImageView) findViewById(R.id.image);
        cover = (ImageView) findViewById(R.id.imageCover);
    }

    /**
     * This method activates a transparent background when an image is clicked on. This is just
     * to provide a nice UI for the user. (Using a background drawable kept giving an oval shape)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            cover.setVisibility(View.VISIBLE);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            cover.setVisibility(View.GONE);
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            cover.setVisibility(View.GONE);
        }

        return super.onTouchEvent(event);
    }
}
