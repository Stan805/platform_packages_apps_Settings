package com.android.settings.datetime;

import android.content.Context;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class ClockSecondsSwitchController extends TogglePreferenceController {

    public ClockSecondsSwitchController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        int mode = Settings.Secure.getInt(mContext.getContentResolver(), "clock_seconds_mode", 0);
        return mode == 1 || mode == 2;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        int value = isChecked ? 1 : 0;
        return Settings.Secure.putInt(mContext.getContentResolver(), "clock_seconds_mode", value);
    }
    
    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system; 
    }
}
