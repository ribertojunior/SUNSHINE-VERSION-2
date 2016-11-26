package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

public class TodayWidgetIntentService extends IntentService {
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;
    private static final int INDEX_CITY_NAME = 4;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        Uri uri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithDate(Utility.getPreferredLocation(this), System.currentTimeMillis());
        Cursor c = getContentResolver().query(uri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (c == null)
            return;
        if (c.moveToFirst()) {
            int weatherArtResourceId = c.getInt(INDEX_WEATHER_ID);
            String description = c.getString(INDEX_SHORT_DESC);
            double maxTemp = c.getDouble(INDEX_MAX_TEMP);
            double minTemp = c.getDouble(INDEX_MIN_TEMP);
            String cityName = c.getString(INDEX_CITY_NAME);

            String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp, Utility.isMetric(this));
            String formattedMINTemperature = Utility.formatTemperature(this, minTemp, Utility.isMetric(this));

            for (int appWidgetID : appWidgetIds) {
                int layoutId;
                int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetID);
                int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
                int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
                if (widgetWidth >= largeWidth) {
                    layoutId = R.layout.widget_today_large;
                    } else if (widgetWidth >= defaultWidth) {
                    layoutId = R.layout.widget_today;
                    } else {
                    layoutId = R.layout.widget_today_small;
                     }
                RemoteViews views = new RemoteViews(getPackageName(), layoutId);
                views.setImageViewResource(R.id.widget_icon, Utility.getArtResourceForWeatherCondition(weatherArtResourceId));


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
                views.setTextViewText(R.id.widget_desc_text_view, description);
                views.setTextViewText(R.id.widget_low_temperature, formattedMINTemperature);
                views.setTextViewText(R.id.widget_city_name_text_view, cityName);
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetID, views);
            }
        }
        c.close();

    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}