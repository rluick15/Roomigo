package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.PlaceJSONParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class OnBoardActivity extends Activity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private AutoCompleteTextView mPlacesField;
    private ArrayAdapter<String> adapter;
    private Double mLat;
    private Double mLng;
    private RadioGroup mGenderGroup;
    private RadioGroup mHasRoomGroup;
    private String mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_on_board);

        mPlacesField = (AutoCompleteTextView) findViewById(R.id.locationField);
        mPlacesField.setOnItemClickListener(this);

        final Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults results) {}

            @Override
            protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    new PlacesTask().execute();
                }
                return null;
            }
        };

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line) {
            public android.widget.Filter getFilter() {
                return filter;
            }
        };

        mPlacesField.setAdapter(adapter);
        adapter.setNotifyOnChange(false);

        mGenderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        mHasRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);

        mGenderGroup.setOnCheckedChangeListener(this);
        mHasRoomGroup.setOnCheckedChangeListener(this);

        Button setPrefButton = (Button) findViewById(R.id.submitButton);
        setPrefButton.setOnClickListener(this);
    }

    /**
     * This method handles the check responses for the radio groups for setting preferences.
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.maleCheckBox:
                mGenderPref = Constants.MALE;
                break;
            case R.id.femaleCheckBox:
                mGenderPref = Constants.FEMALE;
                break;
            case R.id.bothCheckBox:
                mGenderPref = Constants.BOTH;
                break;

            case R.id.yesCheckBox:
                mHasRoom = true;
                break;
            case R.id.noCheckBox:
                mHasRoom = false;
                break;
        }
    }

    /*
     * A method to download json data from url for the location autocomplete
     *
     * @param strUrl the String url of the location being searched
     * */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();

            br.close();
        } catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        } finally{
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return data;
    }

    /**
     * This method gets the location the user selects and extracts the coordinates from it
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPlace = (String) parent.getItemAtPosition(position);

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
     * This method  handles saving the new parse user when the user selects to finish onboarding
     */
    @Override
    public void onClick(View v) {
        if(mGenderGroup.getCheckedRadioButtonId() == -1 ||
                mHasRoomGroup.getCheckedRadioButtonId() == -1 || mLat == null) {
            Toast.makeText(OnBoardActivity.this, getString(R.string.toast_empty_fields), Toast.LENGTH_LONG).show();
        }
        else {
            SharedPreferences pref = getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean(Constants.ALREADY_ONBOARD, true);
            ed.commit();

            ParseUser user = ParseUser.getCurrentUser();
            user.put(Constants.LOCATION, mPlace);
            user.put(Constants.LATITUDE, mLat);
            user.put(Constants.LONGITUDE, mLng);
            user.put(Constants.GENDER_PREF, mGenderPref);
            user.put(Constants.HAS_ROOM, mHasRoom);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_account_created),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(OnBoardActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_error_request),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //
    /**
     * This method fetches all places from GooglePlaces AutoComplete Web Service
     */
    private class PlacesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String place = mPlacesField.getText().toString();
            String data = "";
            String parameters = place.replace(' ', '+') + "&types=geocode&sensor=false&key=" + Constants.PLACES_API_KEY;
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + parameters;

            try{
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);
                places = placeJsonParser.parse(jObject);
            } catch(Exception e){
                Log.d("Exception", e.toString());
            }

            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            adapter.clear();
            for (int i = 0; i < result.size(); i++) {
                adapter.add(result.get(i).get("description"));
            }

            adapter.notifyDataSetChanged();
            mPlacesField.showDropDown();
        }
    }
}
