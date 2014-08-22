package com.shawnaten.networking;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shawnaten.simpleweather.MainActivity;
import com.shawnaten.simpleweather.R;
import com.shawnaten.simpleweather.backend.keysEndpoint.model.Keys;
import com.shawnaten.tools.GeneralAlertDialog;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by shawnaten on 7/17/14.
 */
public class Network implements Callback {
    public static final String NETWORK_ERROR_DIALOG = "networkErrorDialog";

    private static final String
            PLACES_STATUS_OK = "OK", PLACES_STATUS_ZERO_RESULTS = "ZERO_RESULTS", PLACES_STATUS_DENIED = "REQUEST_DENIED";

    private static Network instance;

    private Callback activityCallback;
    private Object o;
    private Response response;
    private RetrofitError error;

    public String getLastLocationName() {
        return lastLocationName;
    }

    public void setLastLocationName(String lastLocationName) {
        this.lastLocationName = lastLocationName;
    }

    private String lastLocationName;

    private Forecast.Service forecastService;
    private Places.Service placesService, detailsService;
    private Keys keys;

    private Network(Callback cb, File networkCacheFile, Keys keys) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkClient okClient;
        Cache networkCache = null;
        this.activityCallback = cb;
        this.keys = keys;

        try {
            networkCache = new Cache(networkCacheFile, 1 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        okHttpClient.setCache(networkCache);
        okClient = new OkClient(okHttpClient);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TimeZone.class, new Deserializers.TimeZoneDeserializer())
                .registerTypeAdapter(Date.class, new Deserializers.DateDeserializer())
                .registerTypeAdapter(Uri.class, new Deserializers.UriDeserializer())
                .create();

        RestAdapter forecastAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.forecast.io/forecast")
                .setClient(okClient)
                .setConverter(new GsonConverter(gson))
                .build();
        forecastService = forecastAdapter.create(Forecast.Service.class);

        RestAdapter placesAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place/autocomplete")
                .setClient(okClient)
                .build();
        placesService = placesAdapter.create(Places.Service.class);

        RestAdapter detailsAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place/details")
                .setClient(okClient)
                .build();
        detailsService = detailsAdapter.create(Places.Service.class);

    }

    public static void setup(Callback cb, File networkCacheFile, Keys keys) {
        if (instance == null || keys.getGoogleAPIKey() == null || keys.getForecastAPIKey() == null)
            instance = new Network(cb, networkCacheFile, keys);
    }

    public boolean isSetup() {
        return keys.getGoogleAPIKey() != null && keys.getForecastAPIKey() != null;
    }

    public void setActivityCallback(Callback cb) {
        this.activityCallback = cb;
        if (activityCallback != null) {
            if (o != null || response != null) {
                activityCallback.success(o, response);
                o = null;
                response = null;
            }
            if (error != null) {
                activityCallback.failure(error);
                error = null;
            }
        }
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public static synchronized Network getInstance() {
        if (instance != null)
            return instance;
        else
            return null;
    }

    public void getForecast(double lat, double lng, String langCode, Callback<Forecast.Response> cb) {
        if (cb == null)
            cb = this;
        forecastService.getForecast(keys.getForecastAPIKey(), lat, lng, langCode, cb);
    }

    public Places.AutocompleteResponse getAutocomplete(String query, String langCode) {
        return placesService.getAutocomplete(keys.getGoogleAPIKey(), query, langCode);
    }

    public void getAutocomplete(String query, String langCode, Callback<Places.AutocompleteResponse> cb) {
        if (cb == null)
            cb = this;
        placesService.getAutocomplete(keys.getGoogleAPIKey(), query, langCode, cb);
    }

    public void getDetails(String placeId, String langCode, Callback<Places.DetailsResponse> cb) {
        if (cb == null)
            cb = this;
        detailsService.getDetails(keys.getGoogleAPIKey(), placeId, langCode, cb);
    }

    public boolean responseOkay(String status, FragmentActivity activity) {

        switch (status) {
            case PLACES_STATUS_OK:
                return true;
            case PLACES_STATUS_ZERO_RESULTS:
                if (activity != null)
                    showNetworkError(activity, activity.getString(R.string.no_search_results));
                return false;
            case PLACES_STATUS_DENIED:
                if (activity != null) {
                    PreferenceManager.getDefaultSharedPreferences(activity).edit()
                        .putString(MainActivity.GOOGLE_API_KEY, null)
                        .putString(MainActivity.FORECAST_API_KEY, null)
                        .apply();
                    GeneralAlertDialog.newInstance(
                            NETWORK_ERROR_DIALOG,
                            activity.getString(R.string.network_error_title),
                            activity.getString(R.string.network_error_message),
                            activity.getString(R.string.network_error_negative),
                            activity.getString(R.string.network_error_positive)
                    ).show(activity.getSupportFragmentManager(), NETWORK_ERROR_DIALOG);
                }
                return false;
            default:
                if (activity != null)
                    showNetworkError(activity, activity.getString(R.string.network_error));
                return false;
        }

    }

    private void showNetworkError(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void success(Object o, Response response) {
        if (activityCallback != null) {
            activityCallback.success(o, response);
        }
        else {
            this.o = o;
            this.response = response;
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if (activityCallback != null) {
            activityCallback.failure(error);
        } else {
            this.error = error;
        }
    }
}
