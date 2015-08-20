package com.richluick.android.roomie.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.richluick.android.roomie.utils.ApiKeys;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;
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
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;

/**
 * Autcomplete TextView with built in google places functionality
 */
public class GooglePlacesAutocompleteTextView extends AutoCompleteTextView {

    public static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=";
    public static final String PLACES_API_PARAMETERS = "&types=geocode&sensor=false&key=";

    public GooglePlacesAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line);

        GooglePlacesAutocompleteTextView googlePlaces = this;
        googlePlaces.setAdapter(adapter);

        WidgetObservable.text(googlePlaces)
                .debounce(200, TimeUnit.MILLISECONDS)
                .map(o -> this.getText().toString())
                .map(s -> s.replace(' ', '+'))
                .map(s -> PLACES_API_BASE_URL + s + PLACES_API_PARAMETERS + ApiKeys.PLACES_API_KEY)
                .flatMap(s -> downloadUrl(s))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                        googlePlaces.showDropDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        adapter.add(s);
                    }
                });
    }

    public static Observable<String> downloadUrl(String strUrl) {
        return Observable.create(subscriber -> {
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
                e.printStackTrace();
            } finally{
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                JSONObject jObject = new JSONObject(data);
                places = placeJsonParser.parse(jObject);
            } catch(Exception e){
                e.printStackTrace();
            }

            if(places != null) {
                for (int i = 0; i < places.size(); i++) {
                    subscriber.onNext(places.get(i).get("description"));
                }
            }

            if(!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }


}
