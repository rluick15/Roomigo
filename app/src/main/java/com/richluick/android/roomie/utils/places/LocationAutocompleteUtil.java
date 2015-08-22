package com.richluick.android.roomie.utils.places;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.richluick.android.roomie.utils.constants.ApiKeys;

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
import rx.schedulers.Schedulers;

/**
 * This class handles setting up the location autocomplete Text view
 */
public class LocationAutocompleteUtil {

    private static ArrayAdapter<String> adapter;

    public static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=";
    public static final String PLACES_API_PARAMETERS = "&types=geocode&sensor=false&key=";

    public static void setAutoCompleteAdapter(Context context, AutoCompleteTextView field) {
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line);

        WidgetObservable.text(field)
            .debounce(500, TimeUnit.MILLISECONDS)
            .map(o -> field.getText().toString())
            .map(s -> s.replace(' ', '+'))
            .map(s -> PLACES_API_BASE_URL + s + PLACES_API_PARAMETERS + ApiKeys.PLACES_API_KEY)
            .flatMap(s -> downloadUrl(s))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<String>() {
                @Override
                public void onCompleted() {
                    adapter.notifyDataSetChanged();
                    field.showDropDown();
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
