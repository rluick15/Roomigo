package com.richluick.android.roomie.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.richluick.android.roomie.R;

/**
 *
 */
public class RoomieFragment extends Fragment {
    private ImageView mProfImageField;
    private ParseFile mProfImage;
    private String mName;
    private String mLocation;
    private String mAboutMe;
    private Boolean mHasRoom;
    private Boolean mSmokes;
    private Boolean mDrinks;
    private Boolean mPets;
    private String mAge;
    private TextView mNameField;
    private TextView mLocationField;
    private TextView mAboutMeTitle;
    private TextView mAboutMeField;
    private TextView mHasRoomField;
    private TextView mSmokesField;

    public RoomieFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomie, container, false);

        mProfImageField = (ImageView) view.findViewById(R.id.profImage);
        mNameField = (TextView) view.findViewById(R.id.nameField);
        mLocationField = (TextView) view.findViewById(R.id.locationField);
        mAboutMeTitle = (TextView) view.findViewById(R.id.aboutMeText);
        mAboutMeField = (TextView) view.findViewById(R.id.aboutMeField);
        mHasRoomField = (TextView) view.findViewById(R.id.hasRoomField);
        mSmokesField = (TextView) view.findViewById(R.id.smokesField);

        return view;
    }

    //the following methods are the setters for the info for the fragment

    public void setProfImage(ParseFile profImage) {
        mProfImage = profImage;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public void setAboutMe(String aboutMe) {
        mAboutMe = aboutMe;
    }

    public void setHasRoom(Boolean hasRoom) {
        mHasRoom = hasRoom;
    }

    public void setSmokes(Boolean smokes) {
        mSmokes = smokes;
    }

    public void setDrinks(Boolean drinks) {
        mDrinks = drinks;
    }

    public void setPets(Boolean pets) {
        mPets = pets;
    }

    public void setAge(String age) {
        mAge = age;
    }

    /*
     * This metod is called once all the variables are reset and it then sets the filed with
     * the new variable for the new Roomie Card
     */
    public void setFields() {
        mNameField.setText(mName + ", " + mAge);
        mLocationField.setText(mLocation);
        mAboutMeTitle.setText("About " + mName);
        mAboutMeField.setText(mAboutMe);

        mSmokesField.setText("");
        if(mSmokes != null) {
            if(mSmokes) {
                mSmokesField.setText("Yes");
            }
            else {
                mSmokesField.setText("No");
            }
        }

        if(mHasRoom) {
            mHasRoomField.setText("Yes");
        }
        else {
            mHasRoomField.setText("No");
        }

        mProfImage.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if(e == null) {
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mProfImageField.setImageBitmap(image);
                }
            }
        });
    }
}
