package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.RadioGroup;

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

public class OnBoardActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private AutoCompleteTextView mPlacesField;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_on_board);

        mPlacesField = (AutoCompleteTextView) findViewById(R.id.locationField);

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

        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        RadioGroup hasRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);

        genderGroup.setOnCheckedChangeListener(this);
        hasRoomGroup.setOnCheckedChangeListener(this);

        Button setPrefButton = (Button) findViewById(R.id.submitButton);
        setPrefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

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

    /** A method to download json data from url */
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

    // Fetches all places from GooglePlaces AutoComplete Web Service
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
