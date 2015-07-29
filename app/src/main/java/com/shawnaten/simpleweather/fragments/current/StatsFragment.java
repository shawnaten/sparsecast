package com.shawnaten.simpleweather.fragments.current;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shawnaten.simpleweather.R;
import com.shawnaten.simpleweather.ui.BaseFragment;
import com.shawnaten.tools.Forecast;
import com.shawnaten.tools.ForecastTools;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Shawn Aten on 7/20/14.
 */
public class StatsFragment extends BaseFragment {
    @Inject
    public StatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Observable<Forecast.Response> forecast = getApp().mainComponent.forecast();
        final View root = inflater.inflate(R.layout.stats, container, false);

        subs.add(forecast.subscribe(new Action1<Forecast.Response>() {
            @Override
            public void call(Forecast.Response response) {
                Forecast.DataPoint currently, hour, today;
                String moisturePref, moistureLabel, moistureValue;

                currently = response.getCurrently();
                hour = response.getHourly().getData()[0];
                today = response.getDaily().getData()[0];

                DateFormat timeForm = ForecastTools.getTimeForm(response.getTimezone());
                SimpleDateFormat shortTimeForm = ForecastTools.getShortTimeForm(response.getTimezone(), 24);
                DecimalFormat percForm = ForecastTools.getPercForm();
                DecimalFormat tempForm = ForecastTools.getTempForm();

                moisturePref = PreferenceManager.getDefaultSharedPreferences(StatsFragment.this.getActivity())
                        .getString(StatsFragment.this.getString(R.string.moisture_key), "humidity");

                switch (moisturePref) {
                    case "dew":
                        moistureLabel = StatsFragment.this.getString(R.string.dew_point);
                        moistureValue = tempForm.format(currently.getDewPoint());
                        break;
                    default:
                        moistureLabel = StatsFragment.this.getString(R.string.humidity);
                        moistureValue = percForm.format(currently.getHumidity());
                }

                ForecastTools.setText((ViewGroup) root, Arrays.asList(R.id.title, R.id.temp_label,
                                R.id.temp, R.id.moisture_label, R.id.moisture, R.id.high_temp,
                                R.id.high_temp_time, R.id.low_temp, R.id.low_temp_time, R.id.time,
                                R.id.currently),
                        Arrays.asList(
                                response.getName(),
                                StatsFragment.this.getString(R.string.temp),
                                tempForm.format(currently.getTemperature()),
                                moistureLabel, moistureValue,
                                tempForm.format(today.getTemperatureMax()),
                                String.format("%s %s", StatsFragment.this.getString(R.string.heavy),
                                        shortTimeForm.format(today.getTemperatureMaxTime())),
                                tempForm.format(today.getTemperatureMin()),
                                String.format("%s %s", StatsFragment.this.getString(R.string.light),
                                        shortTimeForm.format(today.getTemperatureMinTime())),
                                timeForm.format(currently.getTime()),
                                String.format("%s - %s %s", currently.getSummary(),
                                        StatsFragment.this.getString(R.string.feels_like),
                                        tempForm.format(currently.getApparentTemperature()))
                        )
                );

            /*
            if (savedInstanceState == null) {
                WebView webView = (WebView) root.findViewById(R.placeId.weather_icon);
                webView.loadUrl("file:///android_asset/" + response.getCurrently().getIcon() + ".html");
                webView.setBackgroundColor(Color.TRANSPARENT);
            }
            */
            }
        }));

        return root;
    }

}
