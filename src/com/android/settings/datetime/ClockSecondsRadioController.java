package com.android.settings.datetime;

import android.content.Context;
import com.android.settings.R;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

public class ClockSecondsRadioController extends AbstractPreferenceController 
        implements SelectorWithWidgetPreference.OnClickListener, LifecycleObserver, OnResume, OnPause {

    private static final String KEY_QS = "clock_seconds_qs_option";
    private static final String KEY_ALWAYS = "clock_seconds_always_option";
    private static final String KEY_GROUP = "clock_seconds_radio_group";

    private SelectorWithWidgetPreference mQsPref;
    private SelectorWithWidgetPreference mAlwaysPref;
    private final PowerManager mPowerManager;
    private final SettingObserver mSettingObserver;

    public ClockSecondsRadioController(Context context, Lifecycle lifecycle) {
        super(context);
        mPowerManager = context.getSystemService(PowerManager.class);
        
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        mSettingObserver = new SettingObserver(new Handler());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_GROUP; 
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        
        mQsPref = screen.findPreference(KEY_QS);
        mAlwaysPref = screen.findPreference(KEY_ALWAYS);

        if (mQsPref != null) mQsPref.setOnClickListener(this);
        if (mAlwaysPref != null) mAlwaysPref.setOnClickListener(this);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        String key = preference.getKey();
        int value = KEY_QS.equals(key) ? 2 : 1;
        
        Settings.Secure.putInt(mContext.getContentResolver(), "clock_seconds_mode", value);
        
        updateState(preference);
    }

    @Override
    public void updateState(Preference preference) {
        int mode = Settings.Secure.getInt(mContext.getContentResolver(), "clock_seconds_mode", 0);
        boolean isMasterOn = (mode != 0);
        boolean isPowerSave = mPowerManager.isPowerSaveMode();

        if (mQsPref != null) {
            mQsPref.setEnabled(isMasterOn);

            boolean isChecked = (mode == 2) || (mode == 1 && isPowerSave);
            mQsPref.setChecked(isChecked);
        }

        if (mAlwaysPref != null) {
            boolean allowed = isMasterOn && !isPowerSave;
            mAlwaysPref.setEnabled(allowed);
            mAlwaysPref.setChecked((mode == 1) && !isPowerSave);
            if (isPowerSave && isMasterOn) {
                mAlwaysPref.setSummary(mContext.getText(R.string.clock_seconds_always_summary_battery_saver));
            } else {
                mAlwaysPref.setSummary(mContext.getText(R.string.clock_seconds_always_summary));
            }
        }
    }

    @Override
    public void onResume() {
        mSettingObserver.register(mContext.getContentResolver());
        updateState(null); 
    }

    @Override
    public void onPause() {
        mSettingObserver.unregister(mContext.getContentResolver());
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void register(android.content.ContentResolver cr) {
            cr.registerContentObserver(Settings.Secure.getUriFor("clock_seconds_mode"), false, this);
            cr.registerContentObserver(Settings.Global.getUriFor(Settings.Global.LOW_POWER_MODE), false, this);
        }

        public void unregister(android.content.ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateState(null);
        }
    }
}
