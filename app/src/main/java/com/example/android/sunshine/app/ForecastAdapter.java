package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static com.example.android.sunshine.app.ForecastFragment.COL_CITY_NAME;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_CONDITION_ID;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MAX_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MIN_TEMP;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, null, flags);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /*
                Remember that these views are reused as needed.
             */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if (viewType == VIEW_TYPE_TODAY){
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);

        if (view.getId()==R.id.forecast_today) {
            //viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            viewHolder.cityView.setText(cursor.getString(COL_CITY_NAME));
        } else if (view.getId()==R.id.forecast) {
            //viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }
        Glide.with(context)
                .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
                .error(Utility.getArtResourceForWeatherCondition(weatherId))
                .into(viewHolder.iconView);




        viewHolder.descriptionView.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));

        if (mUseTodayLayout) {
            viewHolder.dateView.setText(
                    Utility.getFriendlyDayString(
                            mContext,
                            cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        }else {
            viewHolder.dateView.setText(
                    Utility.getDayName(
                            mContext,
                            cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        }

        viewHolder.highTempView.setText(Utility.formatTemperature(
                mContext,
                cursor.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(mContext)));

        viewHolder.lowTempView.setText(Utility.formatTemperature(
                context,
                cursor.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(mContext)));

    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView cityView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            cityView = (TextView) view.findViewById(R.id.city_name_text_view);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
        }
    }

}