package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

import java.util.concurrent.ExecutionException;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getCanonicalName();

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };
    // these indices must match the projection
    static final int INDEX_WEATHER_ID = 0;
    static final int INDEX_WEATHER_DATE = 1;
    static final int INDEX_WEATHER_CONDITION_ID = 2;
    static final int INDEX_WEATHER_DESC = 3;
    static final int INDEX_WEATHER_MAX_TEMP = 4;
    static final int INDEX_WEATHER_MIN_TEMP = 5;
    private static final int INDEX_CITY_NAME = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new DetailWidgetRemoteViewFactory();
    }

    /*

     */

    public class DetailWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private Cursor mCursor = null;



        @Override
        public void onCreate() {


        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                final long identifyToken = Binder.clearCallingIdentity();
                String location = Utility.getPreferredIcon(DetailWidgetRemoteViewsService.this);
                Uri weatherForLocationUri = WeatherContract.WeatherEntry
                        .buildWeatherLocationWithStartDate(location, System.currentTimeMillis());
                mCursor = getContentResolver().query(weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
                Binder.restoreCallingIdentity(identifyToken);
            }
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position))
                return null;
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_list_item);
            int weatherId = mCursor.getInt(INDEX_WEATHER_CONDITION_ID);
            int weatherArtResourceId = Utility.getIconResourceForWeatherCondition(weatherId);
            Bitmap weatherArtImage = null;
            if (!Utility.usingLocalGraphics(DetailWidgetRemoteViewsService.this)) {
               String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
                       DetailWidgetRemoteViewsService.this, weatherId);
               try {
                   weatherArtImage = Glide.with(DetailWidgetRemoteViewsService.this)
                           .load(weatherArtResourceUrl)
                           .asBitmap()
                           .error(weatherArtResourceId)
                           .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
               } catch (InterruptedException | ExecutionException e) {
                   Log.e(LOG_TAG, "getViewAt: Error retrieving large icons from " + weatherArtResourceUrl, e );
               }
            }
            String description = mCursor.getString(INDEX_WEATHER_DESC);
            long dateInMillis = mCursor.getLong(INDEX_WEATHER_DATE);
            String formattedDate = Utility.getFriendlyDayString(
                    DetailWidgetRemoteViewsService.this, dateInMillis, false);
            double maxTemp = mCursor.getDouble(INDEX_WEATHER_MAX_TEMP);
            double minTemp = mCursor.getDouble(INDEX_WEATHER_MIN_TEMP);
            String cityName = mCursor.getString(INDEX_CITY_NAME);
            String formattedMaxTemperature =
                    Utility.formatTemperature(DetailWidgetRemoteViewsService.this, maxTemp);
            String formattedMinTemperature =
                    Utility.formatTemperature(DetailWidgetRemoteViewsService.this, minTemp);
            if (weatherArtImage != null) {
                rv.setImageViewBitmap(R.id.widget_icon, weatherArtImage);
            } else {
                rv.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                setRemoteContentDescription(rv, description);
            }
            rv.setTextViewText(R.id.widget_detail_date_textview, formattedDate);
            rv.setTextViewText(R.id.widget_desc_text_view, description);
            rv.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
            rv.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);
            rv.setTextViewText(R.id.widget_city_name_text_view, cityName);
            final Intent fillInIntent = new Intent();

            String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);

            Uri weatherUri =
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, dateInMillis);
            fillInIntent.setData(weatherUri);
            rv.setOnClickFillInIntent(R.layout.widget_list_item, fillInIntent);
            return rv;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        private void setRemoteContentDescription(RemoteViews views, String description) {
            views.setContentDescription(R.id.widget_icon, description);
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position))
                return mCursor.getLong(INDEX_WEATHER_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
