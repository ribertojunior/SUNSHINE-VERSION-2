package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
        View rootView = inflater.inflate(R.layout.main, container, false);
        ViewHolder viewHolder = new ViewHolder(rootView);

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
        viewHolder.iconView.setImageResource(getArtResourceForWeatherCondition(weatherId));

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
            BitmapFactory.Options o = Utility.getSize(getActivity(), R.drawable.agulha);
            Matrix matrix = new Matrix();
            matrix.postRotate((float) 90, o.outWidth/2, o.outHeight/2);
            viewHolder.agulha.setScaleType(ImageView.ScaleType.MATRIX);   //required
            viewHolder.agulha.setImageMatrix(matrix);
        }




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
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
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
