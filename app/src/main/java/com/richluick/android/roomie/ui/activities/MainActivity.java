package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.facebook.FacebookRequest;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends BaseActivity {

    private FacebookRequest mRequest;
    private ParseUser mCurrentUser;

    //todo:check if logged in OnResume and add progress bar indicators

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionDetector detector = new ConnectionDetector(this);
        if(!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
        }
        else {
            mCurrentUser = ParseUser.getCurrentUser();
            mRequest = new FacebookRequest(this);
            mRequest.setCurrentFacebookUser();

            ImageView profPicField = (ImageView) findViewById(R.id.profImage);
            new SetProfPic(profPicField).execute();

            TextView usernameField = (TextView) findViewById(R.id.nameField);
            Boolean check = false;
            while (!check) {
                String username = (String) mCurrentUser.get(Constants.NAME);
                usernameField.setText(username);
                if (username != null) {
                    check = true;
                }
            }
        }

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

        private ImageView imageView;

        private SetProfPic(ImageView view) {
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
            imageView.setImageBitmap(bitmap);

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

