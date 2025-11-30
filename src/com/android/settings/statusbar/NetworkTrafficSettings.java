/*
 * Copyright (C) 2024 GrapheneOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.statusbar;

import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_ENABLED = "network_traffic_enabled";
    private static final String KEY_MODE = "network_traffic_mode";
    private static final String KEY_AUTOHIDE = "network_traffic_autohide";
    private static final String KEY_UNITS = "network_traffic_units";
    private static final String KEY_REFRESH_INTERVAL = "network_traffic_refresh_interval";
    private static final String KEY_HIDE_ARROW = "network_traffic_hidearrow";

    private SwitchPreferenceCompat mEnabledPref;
    private ListPreference mModePref;
    private SwitchPreferenceCompat mAutohidePref;
    private ListPreference mUnitsPref;
    private ListPreference mRefreshIntervalPref;
    private SwitchPreferenceCompat mHideArrowPref;

    private PowerManager mPowerManager;
    private final BroadcastReceiver mPowerSaveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
                updatePowerSaveState();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);

        mPowerManager = getContext().getSystemService(PowerManager.class);
        final ContentResolver resolver = getContentResolver();

        // CHANGED: System -> Secure
        mEnabledPref = findPreference(KEY_ENABLED);
        mEnabledPref.setChecked(Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_ENABLED, 0) == 1);
        mEnabledPref.setOnPreferenceChangeListener(this);

        // CHANGED: System -> Secure
        mModePref = findPreference(KEY_MODE);
        int mode = Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_MODE, 0);
        mModePref.setValue(String.valueOf(mode));
        mModePref.setSummary(mModePref.getEntry());
        mModePref.setOnPreferenceChangeListener(this);

        // CHANGED: System -> Secure
        mAutohidePref = findPreference(KEY_AUTOHIDE);
        mAutohidePref.setChecked(Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1);
        mAutohidePref.setOnPreferenceChangeListener(this);

        // CHANGED: System -> Secure
        mUnitsPref = findPreference(KEY_UNITS);
        int units = Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_UNITS, 1);
        mUnitsPref.setValue(String.valueOf(units));
        mUnitsPref.setSummary(mUnitsPref.getEntry());
        mUnitsPref.setOnPreferenceChangeListener(this);

        // CHANGED: System -> Secure
        mRefreshIntervalPref = findPreference(KEY_REFRESH_INTERVAL);
        int interval = Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL, 2);
        mRefreshIntervalPref.setValue(String.valueOf(interval));
        mRefreshIntervalPref.setSummary(mRefreshIntervalPref.getEntry());
        mRefreshIntervalPref.setOnPreferenceChangeListener(this);

        // CHANGED: System -> Secure
        mHideArrowPref = findPreference(KEY_HIDE_ARROW);
        mHideArrowPref.setChecked(Settings.Secure.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_HIDEARROW, 0) == 1);
        mHideArrowPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(mPowerSaveReceiver, 
                new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
        updatePowerSaveState();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mPowerSaveReceiver);
    }

    private void updatePowerSaveState() {
        boolean isPowerSave = mPowerManager.isPowerSaveMode();
        // CHANGED: System -> Secure
        boolean isEnabledInSettings = Settings.Secure.getInt(getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_ENABLED, 0) == 1;

        if (isPowerSave) {
            mEnabledPref.setEnabled(false);
            mEnabledPref.setChecked(false); 
            mEnabledPref.setSummary(R.string.network_traffic_summary_battery_saver);
            
            updateDependencies(false);
        } else {
            mEnabledPref.setEnabled(true);
            mEnabledPref.setChecked(isEnabledInSettings);
            mEnabledPref.setSummary(R.string.network_traffic_enabled_summary);
            
            updateDependencies(isEnabledInSettings);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();

        if (preference == mEnabledPref) {
            boolean enabled = (Boolean) newValue;
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_ENABLED, enabled ? 1 : 0);
            updateDependencies(enabled);
            return true;
        } else if (preference == mModePref) {
            int mode = Integer.parseInt((String) newValue);
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_MODE, mode);
            int index = mModePref.findIndexOfValue((String) newValue);
            mModePref.setSummary(mModePref.getEntries()[index]);
            return true;
        } else if (preference == mAutohidePref) {
            boolean autohide = (Boolean) newValue;
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE, autohide ? 1 : 0);
            return true;
        } else if (preference == mUnitsPref) {
            int units = Integer.parseInt((String) newValue);
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_UNITS, units);
            int index = mUnitsPref.findIndexOfValue((String) newValue);
            mUnitsPref.setSummary(mUnitsPref.getEntries()[index]);
            return true;
        } else if (preference == mRefreshIntervalPref) {
            int interval = Integer.parseInt((String) newValue);
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL, interval);
            int index = mRefreshIntervalPref.findIndexOfValue((String) newValue);
            mRefreshIntervalPref.setSummary(mRefreshIntervalPref.getEntries()[index]);
            return true;
        } else if (preference == mHideArrowPref) {
            boolean hideArrow = (Boolean) newValue;
            // CHANGED: System -> Secure
            Settings.Secure.putInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_HIDEARROW, hideArrow ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateDependencies(boolean enabled) {
        mModePref.setEnabled(enabled);
        mAutohidePref.setEnabled(enabled);
        mUnitsPref.setEnabled(enabled);
        mRefreshIntervalPref.setEnabled(enabled);
        mHideArrowPref.setEnabled(enabled);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.network_traffic_settings);
}