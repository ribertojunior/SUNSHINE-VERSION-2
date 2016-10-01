package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.android.sunshine.app.ForecastFragment.COL_DEGREES;
import static com.example.android.sunshine.app.ForecastFragment.COL_HUMIDITY;
import static com.example.android.sunshine.app.ForecastFragment.COL_PRESSURE;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_ID;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MAX_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MIN_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WIND;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    ShareActionProvider mShareActionProvider;
    String mForecast;
    private static final int LOADER_ID = 15;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " " + FORECAST_SHARE_HASHTAG);
        return shareIntent;

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }
        return new CursorLoader(getActivity(), intent.getData(), ForecastFragment.getForecastColumns(), null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //ArrayList<String> strings = new ArrayList<String>();
        //strings.add(convertCursorRowToUXFormat(data));
        if (!data.moveToFirst()) {return;}

        ViewHolder viewHolder = new ViewHolder(getView());
        int weatherId = data.getInt(COL_WEATHER_ID);
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        viewHolder.descriptionView.setText(data.getString(ForecastFragment.COL_WEATHER_DESC));

        long date =  data.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getDayName(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        viewHolder.dateFriendly.setText(friendlyDateText);
        viewHolder.dateView.setText(dateText);

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


        viewHolder.pressure.setText(
                getActivity()
                        .getString(R.string.format_pressure,data.getDouble(COL_PRESSURE)));

        mForecast = viewHolder.dateView.getText() + " - " + viewHolder.descriptionView.getText()
                + " - " + viewHolder.highTempView.getText() + "/" + viewHolder.lowTempView.getText();

        if (mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView dateFriendly;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidity;
        public final TextView wind;
        public final TextView pressure;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            dateFriendly = (TextView) view.findViewById(R.id.list_item_date_friendly_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidity = (TextView) view.findViewById(R.id.list_item_hum_value_textview);
            wind = (TextView) view.findViewById(R.id.list_item_wind_desc_textview);
            pressure = (TextView)  view.findViewById(R.id.list_item_pressure_value_textview);
        }
    }

}
