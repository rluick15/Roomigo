package com.richluick.android.roomie.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.facebook.AccessToken;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;

/**
 *  This class handles retrieving the current users data from parse and facebook if necessary
 */
public class MainActivityData {

    public Observable<Bitmap> getPictureFromUrl(String profPicURL) {
        return Observable.create(subscriber -> {
            try {
                URL url = new URL(profPicURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                subscriber.onNext(myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }

    /**
     * This helper method takes the result from the Facebook prof pic request and converts it to a
     * byte array and then to a Parse file and then uploads it to parse
     *
     * @param bitmap the bitmap image
     */
    public Observable<Bitmap> saveImageToParse(Bitmap bitmap) {
        return Observable.create(subscriber -> {
            subscriber.onNext(bitmap);

            ParseUser currentUser = ParseUser.getCurrentUser();

            //convert bitmap to byte array and upload to Parse
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            //save the bitmap to parse
            final ParseFile file = new ParseFile(Constants.PROFILE_IMAGE_FILE, byteArray);
            file.saveInBackground((ParseException e) -> {
                if (e == null) {
                    currentUser.put(Constants.PROFILE_IMAGE, file);
                    currentUser.saveInBackground(e1 -> subscriber.onCompleted());
                }
            });
        });
    }

    /**
     * This method gets the users email if this feature was not implemented when the first created
     * an account
     */
    public void getFacebookEmail(final ParseUser user, SimpleFacebook simpleFacebook) {
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.EMAIL)
                .build();

        simpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile response) {
                super.onComplete(response);
                if(response.getEmail() != null) {
                    user.put(Constants.EMAIL, response.getEmail());
                    user.saveInBackground();
                }
            }
        });
    }
}
