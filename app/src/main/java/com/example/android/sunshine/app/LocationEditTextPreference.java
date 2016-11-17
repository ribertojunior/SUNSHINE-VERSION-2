package com.example.android.sunshine.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;


public class LocationEditTextPreference extends EditTextPreference  {

    private static final int DEFAULT_MINIMUN_LOCATION_LENGTH = 2;
    private static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    private int mMinLength;
    static final int PLACE_PICKER_REQUEST = 1;

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.LocationEditTextPreference,0,0
        );
        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MINIMUN_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS){
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Woo! chalalala leloo!", Toast.LENGTH_SHORT).show();
                Context context = getContext();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                Activity settingsActivity = (SettingsActivity) context;
                try {
                    settingsActivity.startActivityForResult(builder.build(settingsActivity), PLACE_PICKER_REQUEST);
                }catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    Log.e(LOG_TAG, "Error on launching PlacePicker");
                    e.printStackTrace();
                }
            }
        });
        return view;
    }



    public LocationEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = (EditText) getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if ( d instanceof AlertDialog) {
                    AlertDialog dialog = (AlertDialog) d;
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (s.toString().length()<mMinLength)
                        positive.setEnabled(false);
                    else
                        positive.setEnabled(true);
                }
            }
        });

    }

}
