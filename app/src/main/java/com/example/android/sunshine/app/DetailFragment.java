package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import static com.example.android.sunshine.app.ForecastFragment.COL_CITY_NAME;
import static com.example.android.sunshine.app.ForecastFragment.COL_DEGREES;
import static com.example.android.sunshine.app.ForecastFragment.COL_HUMIDITY;
import static com.example.android.sunshine.app.ForecastFragment.COL_PRESSURE;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_CONDITION_ID;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MAX_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MIN_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WIND;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    ShareActionProvider mShareActionProvider;
    String mForecast;
    private static final int LOADER_ID = 15;
    Uri mUri;
    static final String DETAIL_URI = "URI";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }

        return inflater.inflate(R.layout.main, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " " + FORECAST_SHARE_HASHTAG);
        return shareIntent;

    }

    public void onLocationChanged(String location) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
                return new CursorLoader(getActivity(), mUri, ForecastFragment.getForecastColumns(), null, null, null);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //ArrayList<String> strings = new ArrayList<String>();
        //strings.add(convertCursorRowToUXFormat(data));
        if (!data.moveToFirst()) {return;}

        ViewHolder viewHolder = new ViewHolder(getView());
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        viewHolder.descriptionView.setText(data.getString(ForecastFragment.COL_WEATHER_DESC));

        long date =  data.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getDayName(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        viewHolder.dateFriendly.setText(friendlyDateText);
        viewHolder.dateView.setText(dateText);
        viewHolder.cityView.setText(data.getString(COL_CITY_NAME));

        viewHolder.highTempView.setText(Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity())));

        viewHolder.lowTempView.setText(Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity())));

        viewHolder.humidity.setText(
                getActivity()
                        .getString(R.string.format_humidity,data.getDouble(COL_HUMIDITY)));

        viewHolder.wind.setText(
                Utility.getFormattedWind(getActivity(), data.getFloat(COL_WIND), data.getFloat(COL_DEGREES)) );



        if (viewHolder.agulha!=null) {
            float degrees = data.getFloat(COL_DEGREES)+360;
            viewHolder.compass.setDegrees(data.getFloat(COL_DEGREES)+360);
            viewHolder.agulha.startAnimation(rotate(degrees));
        }




        viewHolder.pressure.setText(
                getActivity()
                        .getString(R.string.format_pressure,data.getDouble(COL_PRESSURE)));

        mForecast = String.format(getActivity().getString(R.string.format_notification),
                viewHolder.cityView.getText(),
                viewHolder.descriptionView.getText(),
                viewHolder.highTempView.getText(),
                viewHolder.lowTempView.getText());


        if (mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }


    }
    private RotateAnimation rotate(float degree) {
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(1000);
        rotateAnim.setFillAfter(true);
        return rotateAnim;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView cityView;
        public final TextView dateView;
        public final TextView dateFriendly;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidity;
        public final TextView wind;
        public final TextView pressure;
        public final WindCompass compass;
        public final ImageView agulha;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            cityView = (TextView) view.findViewById(R.id.city_name_text_view);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            dateFriendly = (TextView) view.findViewById(R.id.list_item_date_friendly_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidity = (TextView) view.findViewById(R.id.list_item_hum_value_textview);
            wind = (TextView) view.findViewById(R.id.list_item_wind_desc_textview);
            pressure = (TextView)  view.findViewById(R.id.list_item_pressure_value_textview);
            compass = (WindCompass) view.findViewById(R.id.compass);
            agulha = (ImageView) view.findViewById(R.id.agulha);

        }
    }


}
