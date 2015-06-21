package com.richluick.android.roomie.data;

import android.content.Context;
import android.graphics.Bitmap;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  This class handles retrieving the current users data from parse and facebook if necessary
 */
public class MainActivityData {

    private String username;
    private ParseFile profileImage;
    private ParseUser currentUser;
    private MainDataListener mainDataListener;
    private Context context;

    /**
     * This method first checks the connection and then sets the profile image and the username by
     * getting the data from either Facebook or Parse. It is called either during onCreate or if
     * the user clicks refresh in the menu
     */
    public void getDataFromNetwork(Context ctx, ParseUser user, SimpleFacebook simpleFacebook,
                                   MainDataListener listener) {
        mainDataListener = listener; //the listener object
        currentUser = user;
        context = ctx;
        currentUser.fetchIfNeededInBackground();

        //set the username field if ParseUser is not null
        if(currentUser != null) {
            username = (String) currentUser.get(Constants.NAME);
        }

        //if prof pic is null then request from facebook. Should only be on the first login
        if (currentUser != null && currentUser.getParseFile(Constants.PROFILE_IMAGE) == null) {
            if (AccessToken.getCurrentAccessToken() != null) { //check if session opened properly
                facebookRequest(ctx, simpleFacebook);
            }
        }
        else { //get the prof pic from parse
            profileImage = currentUser.getParseFile(Constants.PROFILE_IMAGE);
            mainDataListener.onDataLoadedListener(profileImage.getUrl(), username);
        }
    }

    /**
     * This method contains the facebook request and also sets the users info to parse as well as
     * setting the ui elements
     */
    private void facebookRequest(Context ctx, final SimpleFacebook simpleFacebook) {
        //get simple facebook and add the user properties we are looking to retrieve
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.GENDER)
                .add(Profile.Properties.BIRTHDAY)
                .add(Profile.Properties.ID)
                .add(Profile.Properties.EMAIL)
                .build();

        simpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile response) {
                String id = response.getId();
                String name = response.getFirstName();
                String gender = response.getGender();
                String birthday = response.getBirthday();
                String email = response.getEmail();
                String age = getAge(birthday);

                currentUser.put(Constants.NAME, name);
                currentUser.put(Constants.AGE, age);
                currentUser.put(Constants.GENDER, gender);
                currentUser.put(Constants.EMAIL, email);
                currentUser.saveInBackground();

                if(name != null) {
                    username = name;
                }

                if(id != null) { //display the profile image from facebook
                    mainDataListener.onDataLoadedListener(
                            "https://graph.facebook.com/" + id + "/picture?type=large", username);
                }
            }

            /*
             * Ocassionally an Exception is thrown because the facebook session has been temporarily
             * disconnected. This is an issue with parse and facebook. If this happens, refresh the
             * page by calling the getDataFromNetwork() method and attempt to retrieve the facebook
             * info again.
             */
            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                getDataFromNetwork(context, currentUser, simpleFacebook, mainDataListener);
            }
        });
    }

    /**
     * This method takes a string birthday and calculates the age of the person from it
     *
     * @param birthday the birthdate in string form
     */
    private String getAge(String birthday) {
        Date yourDate;
        String ageString = null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy");
            yourDate = parser.parse(birthday);
            Calendar dob = Calendar.getInstance();
            dob.setTime(yourDate);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            Integer ageInt = age;
            ageString = ageInt.toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return ageString;
    }

    /**
     * This helper method takes the result from the Facebook prof pic request and converts it to a
     * byte array and then to a Parse file and then uploads it to parse
     *
     * @param bitmap the bitmap image
     */
    public void saveImageToParse(Bitmap bitmap) {
        //convert bitmap to byte array and upload to Parse
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        //save the bitmap to parse
        final ParseFile file = new ParseFile(Constants.PROFILE_IMAGE_FILE, byteArray);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    currentUser.put(Constants.PROFILE_IMAGE, file);
                    currentUser.saveInBackground();
                    currentUser.fetchIfNeededInBackground();
                }
            }
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

    /*
     * The listener interface for this class
     */
    public interface MainDataListener {
        void onDataLoadedListener(String profURL, String username);
    }

}
