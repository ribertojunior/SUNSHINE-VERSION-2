package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new DetailWidgetRemoteViewFactory(this.getApplicationContext(), intent);
    }

    /*

     */

    public class DetailWidgetRemoteViewFactory implements RemoteViewsFactory {
        private static final int mCount = 20;
        private Context mContext;
        private int mAppWidgetId;

        public DetailWidgetRemoteViewFactory(Context mContext, Intent intent) {
            this.mContext = mContext;
            mAppWidgetId =
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        @Override
        public void onCreate() {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list);
                rv.setRemoteAdapter(mAppWidgetId, intent);
            }*/

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            final Intent fillInIntent = new Intent();

            String location = Utility.getPreferredLocation(mContext);

            Uri weatherUri =
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis());
            fillInIntent.setData(weatherUri);
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                rv.setDisplayedChild(R.layout.widget_list_item, position);
            }
            rv.setOnClickFillInIntent(R.layout.widget_list_item, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
