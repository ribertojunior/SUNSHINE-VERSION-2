package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

import static com.example.android.sunshine.app.ForecastFragment.COL_CITY_NAME;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_CONDITION_ID;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MAX_TEMP;
import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_MIN_TEMP;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 * */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean mUseTodayLayout;
    private Cursor mCursor;
    final private Context mContext;
    final private ForecastAdapterOnClickHandler mOnClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    ForecastAdapter(Context context, ForecastAdapterOnClickHandler onClickHandler, TextView emptyView, int choiceMode) {
        mContext = context;
        mOnClickHandler = onClickHandler;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);

    }

    void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;

            if (viewType == VIEW_TYPE_TODAY){
                layoutId = R.layout.list_item_forecast_today;
            } else {
                layoutId = R.layout.list_item_forecast;
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return  new ForecastAdapterViewHolder(view);

        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        int weatherId = mCursor.getInt(COL_WEATHER_CONDITION_ID);

        int defaultImage;
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
        }

        if (Utility.usingLocalGraphics(mContext)){
            forecastAdapterViewHolder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId))
                    .into(forecastAdapterViewHolder.mIconView);
        }
        ViewCompat.setTransitionName(forecastAdapterViewHolder.mIconView, "iconView" + position);


        forecastAdapterViewHolder.mDescriptionView.setText(mCursor.getString(ForecastFragment.COL_WEATHER_DESC));
        forecastAdapterViewHolder.mDescriptionView.setContentDescription(
                mContext.getString(R.string.a11y_forecast,
                        mCursor.getString(ForecastFragment.COL_WEATHER_DESC)));

        if (mUseTodayLayout) {
            if (forecastAdapterViewHolder.mCityView!=null) {
                forecastAdapterViewHolder.mCityView.setText(mCursor.getString(COL_CITY_NAME));
            }
            forecastAdapterViewHolder.mDateView.setText(
                    Utility.getFriendlyDayString(
                            mContext,
                            mCursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        }else {
            forecastAdapterViewHolder.mDateView.setText(
                    Utility.getDayName(
                            mContext,
                            mCursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        }

        forecastAdapterViewHolder.mHighTempView.setText(Utility.formatTemperature(
                mContext,
                mCursor.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(mContext)));
        forecastAdapterViewHolder.mHighTempView
                .setContentDescription(
                        mContext.getString(R.string.a11y_high_temp,
                                ""+mCursor.getDouble(COL_WEATHER_MAX_TEMP)));

        forecastAdapterViewHolder.mLowTempView.setText(Utility.formatTemperature(
                mContext,
                mCursor.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(mContext)));
        forecastAdapterViewHolder.mLowTempView
                .setContentDescription(
                        mContext.getString(R.string.a11y_low_temp,
                                ""+mCursor.getDouble(COL_WEATHER_MIN_TEMP)));

        mICM.onBindViewHolder(forecastAdapterViewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstaceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition(){
        return mICM.getSelectedItemPosition();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }



    public Cursor getCursor() {
        return mCursor;
    }


    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mIconView;
        final TextView mCityView;
        final TextView mDateView;
        final TextView mDescriptionView;
        final TextView mHighTempView;
        final TextView mLowTempView;

        ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.detail_icon);
            mCityView = (TextView) view.findViewById(R.id.city_name_text_view);
            mDateView = (TextView) view.findViewById(R.id.detail_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mOnClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ForecastAdapterViewHolder vh);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ForecastAdapterViewHolder) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}