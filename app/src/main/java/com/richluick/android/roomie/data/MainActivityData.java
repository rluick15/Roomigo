package com.richluick.android.roomie.data;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.facebook.Session;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.activities.MainActivity;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  This class handles
 */
public class MainActivityData {

    private String username;
    private ParseFile profileImage;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ParseFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(ParseFile profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * This method first checks the connection and then sets the profile image and the username by
     * getting the data from either Facebook or Parse. It is called either during onCreate or if
     * the user clicks refresh in the menu
     */
    public void getDataFromNetwork(Context ctx, ParseUser currentUser, MainDataListener listener) {
        MainDataListener mainDataListener = listener; //the listener object
        currentUser.fetchInBackground();

        if(currentUser != null) {//set the username field if ParseUser is not null
            String username = (String) currentUser.get(Constants.NAME);

//            if (username != null) {
//                mUsernameField.setText(username);
//                mNavNameField.setText(username);
//            }
        }

        if (!ConnectionDetector.getInstance(this).isConnected()) { //check the connection
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            mConnected = false;
        }
        else { //proceed to set prof pic and settings if connection is active
            mConnected = true;

            //ParseObject yes =

            String username = (String) currentUser.get(Constants.NAME);
            profileImage = currentUser.getParseFile(Constants.PROFILE_IMAGE);

            //if prof pic is null then request from facebook. Should only be on the first login
            if (profileImage == null) {

                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) { //check if session opened properly
                    facebookRequest();
                }
            }
            else { //get the prof pic from parse
                loader.displayImage(profileImage.getUrl(), mProfPicField, MainActivity.this);
                loader.displayImage(profileImage.getUrl(), mNavProfImageField, MainActivity.this);
            }
        }
    }

    /**
     * This method contains the facebook request and also sets the users info to parse as well as
     * setting the ui elements
     */
    private void facebookRequest() {
        //todo:get email
        //get simple facebook and add the user properties we are looking to retrieve
        SimpleFacebook simpleFacebook = SimpleFacebook.getInstance(this);
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.GENDER)
                .add(Profile.Properties.BIRTHDAY)
                .add(Profile.Properties.ID)
                .build();

        simpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile response) {
                String id = response.getId();
                String name = response.getFirstName();
                String gender = response.getGender();
                String birthday = response.getBirthday();
                String age = getAge(birthday);

                mCurrentUser.put(Constants.NAME, name);
                mCurrentUser.put(Constants.AGE, age);
                mCurrentUser.put(Constants.GENDER, gender);
                mCurrentUser.saveInBackground();

                if(id != null) { //display the profile image from facebook
                    loader.displayImage("https://graph.facebook.com/" + id + "/picture?type=large",
                            mProfPicField, MainActivity.this);
                    loader.displayImage("https://graph.facebook.com/" + id + "/picture?type=large",
                            mNavProfImageField, MainActivity.this);
                }

                if (name != null) { //display the username from facebook
                    mUsernameField.setText(name);
                    mNavNameField.setText(name);
                    mNameProgressBar.setVisibility(View.INVISIBLE);
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
                getDataFromNetwork();
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

    public interface MainDataListener {
        void onDataLoadedListener(ParseFile profImage, String username);
    }

}
