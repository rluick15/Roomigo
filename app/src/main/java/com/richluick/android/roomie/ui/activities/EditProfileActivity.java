package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.ui.widgets.ClickableImageView;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EditProfileActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private Boolean mSmokes;
    private Boolean mDrinks;
    private Boolean mPets;
    private Double mLat;
    private Double mLng;
    private String mPlace;
    private ParseUser mCurrentUser;
    private String mLocation;
    private Boolean mImageGallery = true;
    private String mSelectedImage;

    @InjectView(R.id.genderGroup) RadioGroup genderPrefGroup;
    @InjectView(R.id.haveRoomGroup) RadioGroup haveRoomGroup;
    @InjectView(R.id.locationField) AutoCompleteTextView locationField;
    @InjectView(R.id.aboutMe) EditText aboutMeField;
    @InjectView(R.id.yesDrinkCheckBox) CheckBox yesDrink;
    @InjectView(R.id.noDrinkCheckBox) CheckBox noDrink;
    @InjectView(R.id.yesSmokeCheckBox) CheckBox yesSmoke;
    @InjectView(R.id.noSmokeCheckBox) CheckBox noSmoke;
    @InjectView(R.id.yesPetCheckBox) CheckBox yesPet;
    @InjectView(R.id.noPetCheckBox) CheckBox noPet;
    @InjectView(R.id.image1) ClickableImageView image1;
    @InjectView(R.id.image2) ClickableImageView image2;
    @InjectView(R.id.image3) ClickableImageView image3;
    @InjectView(R.id.image4) ClickableImageView image4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.inject(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        mCurrentUser = ParseUser.getCurrentUser();

        genderPrefGroup.setOnCheckedChangeListener(this);
        haveRoomGroup.setOnCheckedChangeListener(this);
        locationField.setOnItemClickListener(this);

        //set Listeners for the images
        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        image4.setOnClickListener(this);

        //set Listeners for the yes/no fields
        yesSmoke.setOnCheckedChangeListener(this);
        noSmoke.setOnCheckedChangeListener(this);
        yesDrink.setOnCheckedChangeListener(this);
        noDrink.setOnCheckedChangeListener(this);
        yesPet.setOnCheckedChangeListener(this);
        noPet.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mImageGallery) {//only run if not coming from an image gallery request
            if (mCurrentUser != null) {
                //get the current options from the user
                mLocation = (String) mCurrentUser.get(Constants.LOCATION);
                mGenderPref = (String) mCurrentUser.get(Constants.GENDER_PREF);
                mHasRoom = (Boolean) mCurrentUser.get(Constants.HAS_ROOM);
                mSmokes = (Boolean) mCurrentUser.get(Constants.SMOKES);
                mDrinks = (Boolean) mCurrentUser.get(Constants.DRINKS);
                mPets = (Boolean) mCurrentUser.get(Constants.PETS);
                String aboutMeText = (String) mCurrentUser.get(Constants.ABOUT_ME);

                //load the images from parse and display them if they are available
                ParseFile profImage1 = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE);
                ParseFile profImage2 = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE2);
                ParseFile profImage3 = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE3);
                ParseFile profImage4 = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE4);

                if (profImage1 != null) {
                    image1.setImage(profImage1.getUrl());
                }
                if (profImage2 != null) {
                    image2.setImage(profImage2.getUrl());
                }
                if (profImage3 != null) {
                    image3.setImage(profImage3.getUrl());
                }
                if (profImage4 != null) {
                    image4.setImage(profImage4.getUrl());
                }

                locationField.setText(mLocation);
                aboutMeField.setText(aboutMeText);

                //set the adapter for the location autocomplete
                LocationAutocompleteUtil.setAutoCompleteAdapter(this, locationField);
                locationField.setListSelection(0);

                //check the corrent gender check box
                switch (mGenderPref) {
                    case Constants.MALE:
                        genderPrefGroup.check(R.id.maleCheckBox);
                        break;
                    case Constants.FEMALE:
                        genderPrefGroup.check(R.id.femaleCheckBox);
                        break;
                    case Constants.BOTH:
                        genderPrefGroup.check(R.id.bothCheckBox);
                        break;
                }

                //check the corrent has room check box
                if (mHasRoom) {
                    haveRoomGroup.check(R.id.yesCheckBox);
                } else {
                    haveRoomGroup.check(R.id.noCheckBox);
                }

                //check the corrent yes/no fields
                setCheckedItems(mSmokes, yesSmoke, noSmoke);
                setCheckedItems(mDrinks, yesDrink, noDrink);
                setCheckedItems(mPets, yesPet, noPet);
            }
            else {
                mImageGallery = true;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /**
     * Get the image from the gallery and save it to parse
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImageGallery = false; //set check to prevent onResume from being called again

        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && data != null) {
                Uri selectedImage = data.getData();
                byte[] byteArray = new byte[0];

                if (Build.VERSION.SDK_INT < 19) { //for SDK levels less than 19
                    String picturePath = getPath(selectedImage);
                    Bitmap bm = BitmapFactory.decodeFile(picturePath);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byteArray = stream.toByteArray();
                }
                else { //anything else
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImage, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap bm = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        byteArray = stream.toByteArray();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                switch (mSelectedImage) { //save to the correct file location and imageview
                    case Constants.PROFILE_IMAGE:
                        saveImage(byteArray, Constants.PROFILE_IMAGE, Constants.PROFILE_IMAGE_FILE, image1);
                        break;
                    case Constants.PROFILE_IMAGE2:
                        saveImage(byteArray, Constants.PROFILE_IMAGE2, Constants.PROFILE_IMAGE_FILE2, image2);
                        break;
                    case Constants.PROFILE_IMAGE3:
                        saveImage(byteArray, Constants.PROFILE_IMAGE3, Constants.PROFILE_IMAGE_FILE3, image3);
                        break;
                    case Constants.PROFILE_IMAGE4:
                        saveImage(byteArray, Constants.PROFILE_IMAGE4, Constants.PROFILE_IMAGE_FILE4, image4);
                        break;
                }
            }
        }
    }

    /**
     * This method saves the image to the current parse user in the correct location
     * @param byteArray This is the bytearray containing the file info for the image
     * @param imageLocation The field on parse that the file will be saved to
     * @param fileName The name of the file
     * @param view The clickableimageview to be set
     */
    private void saveImage(byte[] byteArray, final String imageLocation, String fileName,
                           final ClickableImageView view) {
        //save the bitmap to parse
        final ParseFile file = new ParseFile(fileName, byteArray);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mCurrentUser.put(imageLocation, file);
                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseFile image = mCurrentUser.getParseFile(imageLocation);
                            view.setImage(image.getUrl());
                            mCurrentUser.fetchIfNeededInBackground();
                        }
                    });
                }
            }
        });
    }

    /**
     * This method gets the path to the image in string form from the URI
     *
     * @param selectedImage This is the URI of the selected image to be converted into a String
     */
    private String getPath(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }

    /**
     * This method is called when the activity is created and sets the previously selected
     * values of the radiogroups based upon the users saved profile. This is only used for Yes/No
     * questions
     *
     * @param field This is the boolean value of the questions being checked(true=yes, false=no)
     * @param yes the "yes" checkbox
     * @param no the "no" checkbox
     */
    private void setCheckedItems(Boolean field, CheckBox yes, CheckBox no) {
        if(field != null) {
            if(field) {
                yes.setChecked(true);
            }
            else {
                no.setChecked(true);
            }
        }
    }

    @Override
    public void onClick(final View v) {
        //set the value for the currently selected image and don't disply dialog for image 1
        if(v == image1) {
            mSelectedImage = Constants.PROFILE_IMAGE;
            imageGalleryIntent();
        }
        else {
            //set the value for the currently selected image
            if (v == image2) {
                mSelectedImage = Constants.PROFILE_IMAGE2;
            } else if (v == image3) {
                mSelectedImage = Constants.PROFILE_IMAGE3;
            } else if (v == image4) {
                mSelectedImage = Constants.PROFILE_IMAGE4;
            }

            //display choice dialog for add image/delete
            new MaterialDialog.Builder(this)
                    .items(getResources().getStringArray(R.array.image_choices))
                    .itemColor(R.color.accent)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {//go to gallery
                                imageGalleryIntent();
                            }
                            else if (which == 1) { //remove image reference from user
                                mCurrentUser.remove(mSelectedImage);
                                mCurrentUser.saveInBackground();
                                mCurrentUser.fetchIfNeededInBackground();

                                if (v == image2) {
                                    image2.setDefaultImage();
                                } else if (v == image3) {
                                    image3.setDefaultImage();
                                } else if (v == image4) {
                                    image4.setDefaultImage();
                                }
                            }
                        }
                    }).show();
        }
    }

    /**
     * This method handles the intent to retrive an image from the gallery
     */
    private void imageGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    /**
     * This method handles the check responses for the radio groups for setting preferences.
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            //gender check boxes
            case R.id.maleCheckBox:
                mGenderPref = Constants.MALE;
                break;
            case R.id.femaleCheckBox:
                mGenderPref = Constants.FEMALE;
                break;
            case R.id.bothCheckBox:
                mGenderPref = Constants.BOTH;
                break;

            //has room check boxes
            case R.id.yesCheckBox:
                mHasRoom = true;
                break;
            case R.id.noCheckBox:
                mHasRoom = false;
                break;
        }
    }

    /**
     * This method handles the check responses for the yes/no questions
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        //smoking check boxes
        if(v == yesSmoke) {
            if (isChecked) {
                if (noSmoke.isChecked()) {
                    noSmoke.setChecked(false);
                }
                mSmokes = true;
            }
            else {
                mSmokes = null;
            }
        }
        else if(v == noSmoke) {
            if(isChecked) {
                if (yesSmoke.isChecked()) {
                    yesSmoke.setChecked(false);
                }
                mSmokes = false;
            }
            else {
                mSmokes = null;
            }
        }

        //drink check boxes
        if(v == yesDrink) {
            if (isChecked) {
                if (noDrink.isChecked()) {
                    noDrink.setChecked(false);
                }
                mDrinks = true;
            }
            else {
                mDrinks = null;
            }
        }
        else if(v == noDrink) {
            if(isChecked) {
                if (yesDrink.isChecked()) {
                    yesDrink.setChecked(false);
                }
                mDrinks = false;
            }
            else {
                mDrinks = null;
            }
        }

        //pet check boxes
        if(v == yesPet) {
            if (isChecked) {
                if (noPet.isChecked()) {
                    noPet.setChecked(false);
                }
                mPets = true;
            }
            else {
                mPets = null;
            }
        }
        else if(v == noPet) {
            if(isChecked) {
                if (yesPet.isChecked()) {
                    yesPet.setChecked(false);
                }
                mPets = false;
            }
            else {
                mPets = null;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPlace = (String) parent.getItemAtPosition(position); //grab the selected place object

        //get the lat and lng from the place using the geocoder
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(mPlace, 1);
            if(addresses.size() > 0) {
                mLat = addresses.get(0).getLatitude();
                mLng = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when the user decides to save his profile. It handles whether or not
     * to save a new value of remove the old value from all the yes or no fields
     *
     * @param field This is the boolean value of the fields being saved(true=yes, false=no)
     * @param fieldKey the Parse key of the field to save
     */
    private void saveYesNoFields(Boolean field, String fieldKey) {
        if(field != null) {
            mCurrentUser.put(fieldKey, field);
        }
        else {
            mCurrentUser.remove(fieldKey);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save) {
            updateProfile();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * This method is called when the users clicks on the save button on the actionbar. It updates
     * the profile on the Parse backend.
     */
    private void updateProfile() {
        if (mLat == null && !mLocation.equals(locationField.getText().toString())) {
            Toast.makeText(EditProfileActivity.this,
                    getString(R.string.toast_valid_location), Toast.LENGTH_SHORT).show();
        }
        else {
            //check for connection prior to saving
            if(!ConnectionDetector.getInstance(this).isConnected()) {
                Toast.makeText(EditProfileActivity.this, getString(R.string.no_connection),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                if (mPlace != null) {
                    ParseGeoPoint geoPoint = new ParseGeoPoint(mLat, mLng);
                    mCurrentUser.put(Constants.LOCATION, mPlace);
                    mCurrentUser.put(Constants.GEOPOINT, geoPoint);
                }

                mCurrentUser.put(Constants.GENDER_PREF, mGenderPref);
                mCurrentUser.put(Constants.HAS_ROOM, mHasRoom);
                mCurrentUser.put(Constants.ABOUT_ME, aboutMeField.getText().toString());

                saveYesNoFields(mSmokes, Constants.SMOKES);
                saveYesNoFields(mDrinks, Constants.DRINKS);
                saveYesNoFields(mPets, Constants.PETS);

                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(EditProfileActivity.this, getString(R.string.toast_profile_updated),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, getString(R.string.toast_error_request),
                                    Toast.LENGTH_LONG).show();
                        }
                        mCurrentUser.fetchIfNeededInBackground();
                    }
                });
            }
        }
    }
}
