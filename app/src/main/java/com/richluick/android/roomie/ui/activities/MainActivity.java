package com.richluick.android.roomie.ui.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.facebook.FacebookRequest;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    private FacebookRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequest = new FacebookRequest(this);

        ImageView profPicField = (ImageView) findViewById(R.id.profImage);
        new SetProfPic(this, profPicField).execute();
    }

    /**
     * This Async task requests the profile picture from Facebook and sets it to the imageView
     */
    private class SetProfPic extends AsyncTask<Void, Void, Bitmap> {

        private Context context;
        private ImageView imageView;

        private SetProfPic(Context ctx, ImageView view) {
            this.context = ctx;
            this.imageView = view;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap profPic = null;
            try {
                profPic = mRequest.getProfilePicture();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return profPic;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
}

