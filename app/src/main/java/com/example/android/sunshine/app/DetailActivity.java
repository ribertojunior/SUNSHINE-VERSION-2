/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

import java.util.ArrayList;

import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DESC;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MAX_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MIN_TEMP;

public class DetailActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            default:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String LOG_TAG = DetailFragment.class.getSimpleName();
        ArrayAdapter<String> mDetailAdapter;
        private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
        Uri mForecastUri;
        private static final int LOADER_ID = 15;
        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mForecastUri = Uri.parse(intent.getDataString());
            }
            ArrayList<String> data = new ArrayList<String>();
            //data.add(mForecastUri);
            mDetailAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, data);
            ListView listView = (ListView) rootView.findViewById(R.id.listview_detail);
            listView.setAdapter(mDetailAdapter);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);

            MenuItem menuItem = menu.findItem(R.id.action_share);

            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null");
            }

        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastUri + " " + FORECAST_SHARE_HASHTAG);
            return shareIntent;

        }
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            return new CursorLoader(getActivity(), mForecastUri, ForecastFragment.getForecastColumns(), null, null, sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ArrayList<String> strings = new ArrayList<String>();
            strings.add(convertCursorRowToUXFormat(data));
            mDetailAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, strings);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //mDetailAdapter = null;
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            boolean isMetric = Utility.isMetric(getActivity());
            String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
            return highLowStr;
        }

        /*
            This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
            string.
         */
        private String convertCursorRowToUXFormat(Cursor cursor) {
            cursor.moveToNext();
            String highAndLow = formatHighLows(
                    cursor.getDouble(COL_WEATHER_MAX_TEMP),
                    cursor.getDouble(COL_WEATHER_MIN_TEMP));

            return Utility.formatDate(cursor.getLong(COL_WEATHER_DATE)) +
                    " - " + cursor.getString(COL_WEATHER_DESC) +
                    " - " + highAndLow;
        }
    }
}