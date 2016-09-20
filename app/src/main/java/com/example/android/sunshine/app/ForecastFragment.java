package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Junior on 01/08/2016.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    private void updateWeather(){
        /*
         IDs
         6455259,"name":"Paris"
         3448439,"name":"Sao Paulo"
         3451190,"name":"Rio de Janeiro"
         3448636, sjc
         */
        FetchWeatherTask task = new FetchWeatherTask();
        //task.execute(getResources().getString(R.string.pref_location_default)); default is SJC ID
        //SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences",0); one way to do it
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity()); //another way to do it

        //SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE); doesn't work here
        task.execute(sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)));

       /* Log.v(LOG_TAG,"Package: "+getActivity().getPackageName()+"_preferences\nLocation key: "+
                sharedPreferences.getString(getString(R.string.pref_location_key)
                        ,getString(R.string.pref_location_default))+
                "\nLocation name: "+
                sharedPreferences.getString(getString(R.string.pref_location_name)
                        ,"Default"));*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String dateComplete = ""+cal.getTime(); //Output "Wed Sep 26 14:23:28 EST 2012"
        List<String> fake = new ArrayList<String>();
        fake.add("Today, "+ dateComplete.substring(4,10)+" -> Great!");
        cal.add(Calendar.DAY_OF_MONTH, 1);dateComplete = ""+cal.getTime();
        fake.add("Tomorrow, "+ dateComplete.substring(4,10)+"-> Not Great!");
        cal.add(Calendar.DAY_OF_MONTH, 1);dateComplete = ""+cal.getTime();
        fake.add(dateComplete.substring(4,10)+"-> Great!");
        for (int j = 0; j < 10; j++) {
            cal.add(Calendar.DAY_OF_MONTH, 1);dateComplete = ""+cal.getTime();
            fake.add(dateComplete.substring(4,10)+ "-> Great!");
        }*/

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,R.id.list_item_forecast_textview ,new ArrayList<String>());
        updateWeather();
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = (String) parent.getItemAtPosition(position);
                if (text.equals(null)) {
                    text = "text is null.";
                    Log.e(LOG_TAG, text);
                }
                else{
                    Intent intent = new Intent(getActivity(),DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                updateWeather();
                break;

            case R.id.action_map:
                OpenPreferredLocationInMap();
                break;

            default: return true;

        }

        return super.onOptionsItemSelected(item);
    }
    private void OpenPreferredLocationInMap(){
        //Toast.makeText(getActivity(), "MAP", Toast.LENGTH_SHORT).show();
        //"geo:0,0?q=1600+Amphitheatre+Parkway%2C+CA"
        SharedPreferences sharedPreference = getActivity().getPreferences(Context.MODE_PRIVATE);
        Uri geoLocation = Uri.parse("geo:0,0?q="+sharedPreference.getString(getString(R.string.pref_location_name), "Sao+Jose+dos+Campos"));
        //Log.v(LOG_TAG,geoLocation.toString() );
        //Toast.makeText(getActivity(), geoLocation.toString(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //{"_id":3448636,"name":"Sao Jose dos Campos","country":"BR","coord":{"lon":-45.88694,"lat":-23.17944}}
                //http://api.openweathermap.org/data/2.5/forecast/daily?id=3448636&mode=json&units=metric&cnt=7&APPID=


                final String QUERY_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);
                URL url = new URL(builder.build().toString());
                //Log.v(LOG_TAG, url.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

                //Log.v(LOG_TAG, "Forecast string: "+ forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);

            }catch (JSONException e ){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                mForecastAdapter.clear();
                for(String dayForecastStr : strings) {
                    mForecastAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
            }
        }

        /** The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPreferences.getString(getString(R.string.pref_units_key),
                    getString(R.string.pref_units_default));

            String unit = "C";
            if (!unitType.equals(getString(R.string.pref_units_metrics_summary))){
                high = (high*1.8) + 32;
                low = (low*1.8) + 32;
                unit = "F";
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh+"°"+unit + "/" + roundedLow+"°"+unit;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            final String OWM_CITY = "city";
            final String OWM_NAME = "name";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(getString(R.string.pref_location_name), cityJson.getString(OWM_NAME));

            if(!edit.commit()){
                Log.e(LOG_TAG, "Preferences commit error!");
            }
            //edit.apply();

            //Log.v(LOG_TAG, "Name: "+cityJson.getString(OWM_NAME));
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }


            return resultStrs;

        }

    }
}
