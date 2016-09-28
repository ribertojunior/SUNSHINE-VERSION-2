package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
        FetchWeatherTask task = new FetchWeatherTask(getActivity(), mForecastAdapter);
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

}
