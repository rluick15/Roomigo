package com.richluick.android.roomie.data;

import android.app.Activity;
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
    private ParseUser currentUser;
    private MainDataListener mainDataListener;
    private Context context;

    /**
     * This method first checks the connection and then sets the profile image and the username by
     * getting the data from either Facebook or Parse. It is called either during onCreate or if
     * the user clicks refresh in the menu
     */
    public void getDataFromNetwork(Context ctx, ParseUser user, MainDataListener listener) {
        mainDataListener = listener; //the listener object
        currentUser = user;
        context = ctx;
        currentUser.fetchIfNeededInBackground();

        //set the username field if ParseUser is not null and the username has not been set
        if(currentUser != null && currentUser.get(Constants.NAME) == null) {
            username = (String) currentUser.get(Constants.NAME);
        }

        //if prof pic is null then request from facebook. Should only be on the first login
        if (currentUser.getParseFile(Constants.PROFILE_IMAGE) == null) {
            Session session = Session.getActiveSession();
            if (session != null && session.isOpened()) { //check if session opened properly
                facebookRequest(ctx);
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
    private void facebookRequest(Context ctx) {
        //get simple facebook and add the user properties we are looking to retrieve
        SimpleFacebook simpleFacebook = SimpleFacebook.getInstance((Activity) context);
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
                //currentUser.put(Constants.EMAIL, email);
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
                getDataFromNetwork(context, currentUser, mainDataListener);
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
        void onDataLoadedListener(String profURL, String username);
    }

}
