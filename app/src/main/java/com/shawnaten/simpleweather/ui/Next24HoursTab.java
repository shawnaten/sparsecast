package com.shawnaten.simpleweather.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.charts.LineChart;
import com.shawnaten.simpleweather.R;
import com.shawnaten.simpleweather.ui.widget.VerticalWeatherBar;
import com.shawnaten.tools.Charts;
import com.shawnaten.tools.Forecast;
import com.shawnaten.tools.ForecastTools;
import com.shawnaten.tools.LocalizationSettings;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class Next24HoursTab extends Tab {
    private static int[] statIds = {R.string.wind, R.string.humidity, R.string.dew_point,
            R.string.pressure, R.string.visibility};
    private View nextHourAndStatsSection;
    private View nextHourSection;
    private GridLayout statsSection;
    private View next24HoursSection;
    private VerticalWeatherBar verticalWeatherBar;

    public static Next24HoursTab newInstance(String title, int layout) {
        Bundle args = new Bundle();
        Next24HoursTab tab = new Next24HoursTab();
        args.putString(TabAdapter.TAB_TITLE, title);
        args.putInt(TAB_LAYOUT, layout);
        tab.setArguments(args);
        return tab;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionMenu fam;
        FloatingActionButton item1, item2;

        fam = getMainActivity().getFam();

        item1 = new FloatingActionButton(fam.getContext());
        item1.setButtonSize(FloatingActionButton.SIZE_MINI);
        item1.setColorNormalResId(R.color.white);
        item1.setColorPressedResId(R.color.button_pressed_light);
        item1.setImageResource(R.drawable.ic_trending_up_black_24dp);
        item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextHourSection.setVisibility(View.VISIBLE);
                statsSection.setVisibility(View.INVISIBLE);
            }
        });
        fabs.add(item1);
        getMainActivity().addFab(item1);

        item2 = new FloatingActionButton(fam.getContext());
        item2.setButtonSize(FloatingActionButton.SIZE_MINI);
        item2.setColorNormalResId(R.color.white);
        item2.setColorPressedResId(R.color.button_pressed_light);
        item2.setImageResource(R.drawable.ic_list_black_24dp);
        item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statsSection.setVisibility(View.VISIBLE);
                nextHourSection.setVisibility(View.INVISIBLE);
            }
        });
        fabs.add(item2);
        getMainActivity().addFab(item2);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nextHourAndStatsSection = view.findViewById(R.id.next_hour_and_stats_section);
        nextHourSection = nextHourAndStatsSection.findViewById(R.id.next_hour_section);
        statsSection = (GridLayout) nextHourAndStatsSection.findViewById(R.id.stats_section);
        next24HoursSection = view.findViewById(R.id.next_24_hours_section);
        verticalWeatherBar = (VerticalWeatherBar) next24HoursSection
                .findViewById(R.id.vertical_weather_bar);

        ViewGroup.LayoutParams layoutParams = nextHourAndStatsSection.getLayoutParams();
        layoutParams.height = screenHeight - screenWidth;
        nextHourAndStatsSection.setLayoutParams(layoutParams);

        next24HoursSection.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                ViewGroup.LayoutParams layoutParams1 = next24HoursSection.getLayoutParams();
                layoutParams1.height = screenHeight - getMinPhotoHeight();
                next24HoursSection.setLayoutParams(layoutParams1);
            }
        });

        GridLayout.Spec col1 = GridLayout.spec(0, 1f);
        GridLayout.Spec col2 = GridLayout.spec(1, 1f);
        LayoutInflater inflater = LayoutInflater.from(statsSection.getContext());
        for (int i = 0; i < statIds.length; i++) {
            GridLayout.Spec row = GridLayout.spec(i, 1f);
            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();

            TextView label = (TextView) inflater.inflate(R.layout.stats_label, statsSection, false);
            TextView text = (TextView) inflater.inflate(R.layout.stats_text, statsSection, false);
            text.setId(statIds[i]);

            gridParams.columnSpec = col1;
            gridParams.rowSpec = row;
            gridParams.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER);
            label.setText(statIds[i]);
            statsSection.addView(label, gridParams);

            gridParams = new GridLayout.LayoutParams();
            gridParams.columnSpec = col2;
            gridParams.rowSpec = row;
            gridParams.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER);
            statsSection.addView(text, gridParams);
        }
    }

    @Override
    protected void fabSetup() {
        super.fabSetup();

        if (getUserVisibleHint() && isVisible()) {
            FloatingActionMenu fam = getMainActivity().getFam();

            if (nextHourSection.getVisibility() != View.GONE) {
                if (scroll.getScrollY() > screenHeight - screenWidth) {
                    if (!fam.isMenuButtonHidden())
                        fam.hideMenuButton(true);
                } else {
                    if (fam.isMenuButtonHidden())
                        fam.showMenuButton(true);
                }
            } else {
                if (!fam.isMenuButtonHidden())
                    fam.hideMenuButton(true);
            }
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        super.onScrollChanged(deltaX, deltaY);

        fabSetup();
    }

    @Override
    public void onNewData(Object data) {
        super.onNewData(data);

        if (isVisible() && Forecast.Response.class.isInstance(data)) {
            Forecast.Response forecast = (Forecast.Response) data;

            verticalWeatherBar.setData(forecast);

            Forecast.DataPoint currently = forecast.getCurrently();
            Forecast.DataBlock hourly = forecast.getHourly();

            View root = getView();

            LineChart chart = (LineChart) root.findViewById(R.id.precipitation_chart);
            TextView nextHourSummary = (TextView) root.findViewById(R.id.next_hour_summary);
            TextView nearestStorm = (TextView) root.findViewById(R.id.nearest_storm);
            TextView next24HourSummary = (TextView)
                    root.findViewById(R.id.next_24_hours_summary);
            TextView sunText1 = (TextView) root.findViewById(R.id.sun_text_1);
            TextView sunText2 = (TextView) root.findViewById(R.id.sun_text_2);

            if (forecast.getMinutely() != null) {
                statsSection.setVisibility(View.INVISIBLE);
                nextHourSection.setVisibility(View.VISIBLE);
                fabSetup();
                nextHourSummary.setText(forecast.getMinutely().getSummary());
                Charts.setPrecipitationGraph(getActivity(), chart,
                        forecast.getMinutely().getData(), forecast.getTimezone());
            } else {
                statsSection.setVisibility(View.VISIBLE);
                nextHourSection.setVisibility(View.GONE);
                fabSetup();
            }

            if ((int) currently.getNearestStormDistance() > 0) {
                nearestStorm.setVisibility(View.VISIBLE);
                nearestStorm.setText(String.format(
                        "(%s: %d %s %s)",
                        getString(R.string.nearest_storm),
                        (int) currently.getNearestStormDistance(),
                        getString(LocalizationSettings.getDistanceUnit()),
                        getString(ForecastTools.getWindString(currently
                                .getNearestStormBearing()))
                ));
            } else
                nearestStorm.setVisibility(View.GONE);

            for (int id : statIds) {
                DecimalFormat intForm = ForecastTools.getIntForm();
                DecimalFormat tempForm = ForecastTools.getTempForm();
                DecimalFormat percForm = ForecastTools.getPercForm();

                String text = "";
                TextView textView = (TextView) statsSection.findViewById(id);

                switch (id) {
                    case R.string.wind:
                        text = String.format("%s %s %s",
                                intForm.format(currently.getWindSpeed()),
                                getString(LocalizationSettings.getSpeedUnit()),
                                getString(ForecastTools.getWindString(currently.getWindBearing())));
                        break;
                    case R.string.humidity:
                        text = percForm.format(currently.getHumidity());
                        break;
                    case R.string.dew_point:
                        text = tempForm.format(currently.getDewPoint());
                        break;
                    case R.string.pressure:
                        text = String.format("%s %s",
                                intForm.format(currently.getPressure()),
                                getString(LocalizationSettings.getPressureUnit()));
                        break;
                    case R.string.visibility:
                        text = String.format("%s %s",
                                intForm.format(currently.getVisibility()),
                                getString(LocalizationSettings.getDistanceUnit()));
                        break;
                }

                textView.setText(text);
            }

            next24HourSummary.setText(hourly.getSummary());

            verticalWeatherBar.setData(forecast);

            Date nowTime, sunTime1, sunTime2;
            Forecast.DataPoint today, tomorrow;

            today = forecast.getDaily().getData()[0];
            tomorrow = forecast.getDaily().getData()[1];

            nowTime = forecast.getCurrently().getTime();

            int sunString;

            if (nowTime.before(today.getSunriseTime())) {
                sunTime1 = today.getSunriseTime();
                sunString = R.string.sunrise;
                sunTime2 = today.getSunsetTime();
            } else if (nowTime.before(today.getSunsetTime())) {
                sunTime1 = today.getSunsetTime();
                sunString = R.string.sunset;
                sunTime2 = today.getSunriseTime();
            } else {
                sunTime1 = tomorrow.getSunriseTime();
                sunString = R.string.sunrise;
                sunTime2 = tomorrow.getSunsetTime();
            }

            long difference = sunTime1.getTime() - nowTime.getTime();
            long hours = difference / 3600000;
            difference -= hours * 3600000;
            long minutes = difference / 60000;

            if (hours == 0 && minutes == 0) {
                if (sunString == R.string.sunrise) {
                    sunTime1 = today.getSunsetTime();
                    sunString = R.string.sunset;
                    sunTime2 = today.getSunriseTime();
                } else {
                    sunTime1 = tomorrow.getSunriseTime();
                    sunString = R.string.sunrise;
                    sunTime2 = tomorrow.getSunsetTime();
                }
            }

            difference = sunTime1.getTime() - nowTime.getTime();
            hours = difference / 3600000;
            difference -= hours * 3600000;
            minutes = difference / 60000;

            String text = String.format("%s %s", getString(sunString), getString(R.string.in));

            if (hours > 0) {
                text += String.format(" %d %s", hours, hours > 1 ? getString(R.string.hours_short)
                        : getString(R.string.hour_short));
            }

            if (minutes > 0) {
                text += String.format(" %d %s", minutes, minutes > 1 ? getString(R.string.minutes_short)
                        : getString(R.string.minute_short));
            }

            DateFormat dateFormat = ForecastTools.getTimeForm(forecast.getTimezone());

            text += String.format(" (%s)", dateFormat.format(sunTime1.getTime()));

            sunText1.setText(text);

            difference = Math.abs(sunTime1.getTime() - sunTime2.getTime());
            hours = difference / 3600000;
            difference -= hours * 3600000;
            minutes = difference / 60000;

            text = "(";

            if (hours > 0) {
                text += String.format("%d %s ", hours, hours > 1 ? getString(R.string.hours_short)
                        : getString(R.string.hour_short));
            }

            if (minutes > 0) {
                text += String.format("%d %s ", minutes, minutes > 1 ? getString(R.string.minutes_short)
                        : getString(R.string.minute_short));
            }

            text += String.format("%s %s)", getString(R.string.of), getString(R.string.daylight));

            sunText2.setText(text);
        }
    }
}
