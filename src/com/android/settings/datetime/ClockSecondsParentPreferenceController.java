package com.android.settings.datetime;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class ClockSecondsParentPreferenceController extends TogglePreferenceController 
        implements LifecycleObserver, OnStart, OnStop {

    private final PowerManager mPowerManager;
    private final SettingObserver mSettingObserver;
    private Preference mPreference;

    public ClockSecondsParentPreferenceController(Context context, String key) {
        super(context, key);
        mPowerManager = context.getSystemService(PowerManager.class);
        mSettingObserver = new SettingObserver(new Handler());
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
        int val = isChecked ? 1 : 0;
        return Settings.Secure.putInt(mContext.getContentResolver(), "clock_seconds_mode", val);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public CharSequence getSummary() {
        if (mPowerManager.isPowerSaveMode()) {
            return mContext.getText(R.string.clock_seconds_summary_battery_saver);
        }
        return mContext.getText(R.string.clock_seconds_summary_default);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshSummary(preference);
    }

    @Override
    public void onStart() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
        }
    }

    @Override
    public void onStop() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void register(android.content.ContentResolver cr) {
            cr.registerContentObserver(Settings.Global.getUriFor(Settings.Global.LOW_POWER_MODE), false, this);
            cr.registerContentObserver(Settings.Secure.getUriFor("clock_seconds_mode"), false, this);
        }

        public void unregister(android.content.ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (mPreference != null) {
                updateState(mPreference);
            }
        }
    }
}
