package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import static com.example.android.sunshine.app.Utility.isOnline;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener  {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ForecastAdapter mForecastAdapter;
    private static final int LOADER_ID = 14;
    public static final String SELECTED_KEY = "position";
    private double mLat;
    private double mLong;
    private static final int OFFLINE = 54;
    private boolean mUseTodayLayout, mAutoSelectView;
    private int mChoiceMode;
    private RecyclerView.OnScrollListener onScrollListener;
    private boolean mHoldForTransition;
    private long mInitialSelectedDate = -1;
    private static final int FORECAST_LOADER = 0;


    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_WIND = 10;
    static final int COL_DEGREES = 11;
    static final int COL_PRESSURE = 12;
    static final int COL_CITY_NAME = 13;



    public ForecastFragment() {
    }



    public void onLocationChanged() {
        //updateWeather();

        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ViewHolder viewHolder = new ViewHolder(rootView);
        mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ForecastAdapterViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity())
                        .onItemSelected(WeatherContract.WeatherEntry
                                .buildWeatherLocationWithDate(locationSetting,
                                        date), vh);

            }
        }, viewHolder.mEmptyView, mChoiceMode);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        //viewHolder.mRecyclerView.setEmptyView(viewHolder.mEmptyView);
        viewHolder.mRecyclerView.setAdapter(mForecastAdapter);
        viewHolder.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewHolder.mRecyclerView.setHasFixedSize(true);
        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (parallaxView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                onScrollListener = new RecyclerView.OnScrollListener() {

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0)
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() -dy/2));
                        else
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() -dy/3));

                    }
                };
                viewHolder.mRecyclerView.addOnScrollListener(onScrollListener);
            }
        }else {
            Log.d(LOG_TAG, "onCreateView on Forecasta Fragment: parallaxView is null.");
        }




        //viewHolder.mRecyclerView.setItemChecked(0, true);
        //SunshineSyncAdapter.syncImmediately(getActivity());
        if (savedInstanceState != null ) {
            mForecastAdapter.onRestoreInstanceState(savedInstanceState);
        }
        rootView.setTag(viewHolder);
        return rootView;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment, 0, 0);
        mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        mHoldForTransition = a.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        a.recycle();
    }

    @Override
    public void onResume() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (onScrollListener != null && getView() != null) {
            ViewHolder view = (ViewHolder) getView().getTag();
            view.mRecyclerView.removeOnScrollListener(onScrollListener);
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        mForecastAdapter.onSaveInstaceState(outState);
        super.onSaveInstanceState(outState);
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
            case R.id.action_map:
                openPreferredLocationInMap();
                break;
            default: return true;



        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        updateEmptyView();
        final ViewHolder viewHolder = (ViewHolder) getView().getTag();
        if (data.moveToNext()) {
            mLat = data.getDouble(COL_COORD_LAT);
            mLong = data.getDouble(COL_COORD_LONG);


            if(data.getPosition()==0) {
                if (getActivity().findViewById(R.id.weather_detail_container) != null) {
                    final int WHAT = 1;
                    final Cursor c = data;
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == WHAT) {
                                c.moveToFirst();
                                String locationSetting = Utility.getPreferredLocation(getActivity());
                                Bundle args = new Bundle();
                                args.putParcelable(DetailFragment.DETAIL_URI,
                                        WeatherContract.WeatherEntry
                                                .buildWeatherLocationWithDate(locationSetting,
                                                        c.getLong(COL_WEATHER_DATE)));
                                DetailFragment detailFragment = new DetailFragment();
                                detailFragment.setArguments(args);
                                FragmentTransaction ft = getActivity()
                                        .getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.weather_detail_container, detailFragment);
                                ft.commit();
                            }
                        }
                    };
                    handler.sendEmptyMessage(WHAT);
                    //viewHolder.mRecyclerView.setItemChecked(0, true);
                }
            }
            viewHolder.mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (viewHolder.mRecyclerView.getChildCount() > 0) {
                        viewHolder.mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = mForecastAdapter.getSelectedItemPosition();
                        /* I've changed all this stuff to look better */
                        if (RecyclerView.NO_POSITION == position) {
                            position = 0;
                            if (mInitialSelectedDate != -1) {
                                Cursor data = mForecastAdapter.getCursor();
                                int dateColumn = data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                                while (data.moveToNext()) {
                                    if (data.getLong(dateColumn) == mInitialSelectedDate) {
                                        position = data.getPosition();
                                        break;
                                    }
                                }
                            }

                        }
                        viewHolder.mRecyclerView.smoothScrollToPosition(position);
                        RecyclerView.ViewHolder vh = viewHolder.mRecyclerView.findViewHolderForAdapterPosition(position);
                        if (null != vh && mAutoSelectView) {
                            mForecastAdapter.selectView(vh);
                        }
                        if (mHoldForTransition) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        } else {
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mForecastAdapter.swapCursor(null);
    }

    public void setInitialSelectedDate(long initialSelectedDate) {
        mInitialSelectedDate = initialSelectedDate;
    }


    public static String[] getForecastColumns() {
        return FORECAST_COLUMNS;
    }

    private void openPreferredLocationInMap(){

        Uri geoLocation = Uri.parse("geo:"+mLat+","+mLong+"?z=11");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "openPreferredLocationInMap: Couldn't call the map. No receiving apps installed!");
        }
    }

    private void updateEmptyView(){
        if (mForecastAdapter.getItemCount() == 0) {
            ViewHolder viewHolder = (ViewHolder) getView().getTag();
            if (viewHolder.mEmptyView != null) {
                String text = "";
                @SunshineSyncAdapter.LocationStatus int status = Utility.getLocationStatus(getActivity());
                switch (status) {
                    case SunshineSyncAdapter.LOCATION_STATUS_OK : {
                        text = "";
                        break;
                    }
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:{
                        text = getString(R.string.empty_forecast_list_server_down);
                        break;
                    }
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID: {
                        text = getString(R.string.empty_forecast_list_server_error);
                        break;
                    }
                    case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN: {
                        text = getString(R.string.empty_forecast_list_server_unknown);
                        break;
                    }
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:{
                        text = getString(R.string.empty_forecast_list_invalid_location);
                        break;
                    }
                    default: {
                        if (!isOnline(getActivity())) {
                            text = getString(R.string.network_error);
                        }
                        break;
                    }
                }

                //((TextView)getView().findViewById(R.id.empty_view)).setText(text);
                viewHolder.mEmptyView.setText(text);
            }
        }
    }




    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ForecastAdapterViewHolder vh);
    }

    static class ViewHolder {
        final RecyclerView mRecyclerView;
        final TextView mEmptyView;
        ViewHolder(View view) {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.recycleview_forecast);
            mEmptyView = (TextView) view.findViewById(R.id.listview_forecast_empty);

        }
    }

}
