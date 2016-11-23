package com.example.android.sunshine.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.DetailActivity;


public class DetailWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int appWidgetId = widgetManager.getAppWidgetIds()
        for (int i) {
            RemoteViews remoteViews = new RemoteViews(parcel[i]);
            widgetManager.notifyAppWidgetViewDataChanged(
                    widgetManager.getAppWidgetIds(new ComponentName(context,
                            TodayWidgetProvider.class)),
                    );
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            for (int i = 0; i <appWidgetIds.length; i++) {

                Intent clickIntent = new Intent(context, DetailActivity.class);
                PendingIntent clickPendingIntent = TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(clickIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                //views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent);
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
