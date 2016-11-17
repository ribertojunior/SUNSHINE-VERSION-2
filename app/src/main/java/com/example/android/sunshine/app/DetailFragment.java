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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
    String mForecast;
    private static final int LOADER_ID = 15;
    Uri mUri;
    private boolean mTransitionAnimation;
    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = args.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        return inflater.inflate(R.layout.main, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }

    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " " + FORECAST_SHARE_HASHTAG);
        return shareIntent;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.action_settings:

                break;
            case R.id.action_share:

                break;
            default:
                return true;

        }

        return super.onOptionsItemSelected(item);
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
        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView ) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View) vp).setVisibility(View.VISIBLE);
        }
        ViewHolder viewHolder = new ViewHolder(getView());
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        //viewHolder.mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                .error(Utility.getArtResourceForWeatherCondition(weatherId))
                .into(viewHolder.iconView);

        viewHolder.descriptionView.setText(data.getString(ForecastFragment.COL_WEATHER_DESC));

        long date = data.getLong(COL_WEATHER_DATE);
        //String friendlyDateText = Utility.getDayName(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        //viewHolder.dateFriendly.setText(friendlyDateText);
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
                        .getString(R.string.format_humidity, data.getDouble(COL_HUMIDITY)));

        viewHolder.wind.setText(
                Utility.getFormattedWind(getActivity(), data.getFloat(COL_WIND), data.getFloat(COL_DEGREES)));

        viewHolder.pressure.setText(
                getActivity()
                        .getString(R.string.format_pressure, data.getDouble(COL_PRESSURE)));

        mForecast = String.format(getActivity().getString(R.string.format_notification),
                viewHolder.cityView.getText(),
                viewHolder.descriptionView.getText(),
                viewHolder.highTempView.getText(),
                viewHolder.lowTempView.getText());

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }


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
        public final TextView cityView;
        public final TextView dateView;
        //public final TextView dateFriendly;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidity;
        public final TextView wind;
        public final TextView pressure;
        /*public final WindCompass compass;
        public final ImageView agulha;*/

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            cityView = (TextView) view.findViewById(R.id.city_name_text_view);//new TextView(view.getContext());
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            //dateFriendly = (TextView) view.findViewById(R.id.list_item_date_friendly_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            humidity = (TextView) view.findViewById(R.id.detail_humidity_textview);
            wind = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressure = (TextView)  view.findViewById(R.id.detail_pressure_textview);
            /*compass = (WindCompass) view.findViewById(R.id.compass);
            agulha = (ImageView) view.findViewById(R.id.agulha);*/

        }
    }


}
