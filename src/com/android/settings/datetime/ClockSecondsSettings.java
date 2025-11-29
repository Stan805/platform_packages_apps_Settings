package com.android.settings.datetime;

import android.app.settings.SettingsEnums;
import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class ClockSecondsSettings extends DashboardFragment {

    private static final String TAG = "ClockSecondsSettings";

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.clock_seconds_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DATE_TIME;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        // 1. Add the Master Switch Controller
        controllers.add(new ClockSecondsSwitchController(context, "clock_seconds_main_switch"));
        
        // 2. Add the Radio Group Controller
        controllers.add(new ClockSecondsRadioController(context, getSettingsLifecycle()));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.clock_seconds_settings);
}
