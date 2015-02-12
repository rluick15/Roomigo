package com.richluick.android.roomie.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.facebook.FacebookRequest;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.ImageHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    private FacebookRequest mRequest;
    private ParseUser mCurrentUser;

    //todo:check if logged in OnResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        setContentView(R.layout.activity_main);



        mCurrentUser = ParseUser.getCurrentUser();
        mRequest = new FacebookRequest(this);
        mRequest.setCurrentFacebookUser();

        ImageView profPicField = (ImageView) findViewById(R.id.profImage);
        new SetProfPic(this, profPicField).execute();

        TextView usernameField = (TextView) findViewById(R.id.nameField);
        String username = (String) mCurrentUser.get(Constants.NAME);
        usernameField.setText(username);

        RelativeLayout profileButton = (RelativeLayout) findViewById(R.id.profileSplace);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout searchButton = (RelativeLayout) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout chatButton = (RelativeLayout) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
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
                Boolean check = false;
                while(!check) {
                    profPic = mRequest.getProfilePicture();
                    if(profPic != null) {
                        check = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return profPic;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Bitmap roundedBitmap = ImageHelper.getRoundedCornerBitmap(bitmap, 100);
            imageView.setImageBitmap(roundedBitmap);

            //convert bitmap to byte array and upload to Parse
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            final ParseFile file = new ParseFile(Constants.PROFILE_IMAGE_FILE, byteArray);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null) {
                        mCurrentUser.put(Constants.PROFILE_IMAGE, file);
                        mCurrentUser.saveInBackground();
                    }
                }
            });
        }
    }
}

