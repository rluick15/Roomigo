package com.richluick.android.roomie.ui.fragments;

import android.app.Activity;
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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RoomieFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RoomieFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ImageView mProfImageField;
    private ParseFile mProfImage;
    private String mName;
    private String mLocation;
    private String mAboutMe;
    private Boolean mHasRoom;
    private String mAge;

    public RoomieFragment() {} // Required empty public constructor

    public RoomieFragment(Boolean hasRoom, String aboutMe, String location, String name,
                          ParseFile profImage, String age) {
        this.mHasRoom = hasRoom;
        this.mAboutMe = aboutMe;
        this.mLocation = location;
        this.mName = name;
        this.mProfImage = profImage;
        this.mAge = age;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomie, container, false);

        mProfImageField = (ImageView) view.findViewById(R.id.profImage);
        TextView nameField = (TextView) view.findViewById(R.id.nameField);
        TextView locationField = (TextView) view.findViewById(R.id.locationField);
        TextView aboutMeTitle = (TextView) view.findViewById(R.id.aboutMeText);
        TextView aboutMeField = (TextView) view.findViewById(R.id.aboutMeField);
        TextView hasRoomField = (TextView) view.findViewById(R.id.hasRoomField);

        nameField.setText(mName + ", " + mAge);
        locationField.setText(mLocation);
        aboutMeTitle.setText("About " + mName);
        aboutMeField.setText(mAboutMe);

        if(mHasRoom) {
            hasRoomField.setText("Yes");
        }
        else {
            hasRoomField.setText("No");
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

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
    }

}
