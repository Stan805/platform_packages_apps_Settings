package com.android.settings.display;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;

import java.util.List;

public class BlackThemePreferenceController extends TogglePreferenceController {

    private static final String TAG = "BlackThemePrefCtrl";
    private static final String OVERLAY_PACKAGE = "com.android.overlay.customization.blacktheme";

    private final IOverlayManager mOverlayManager;
    private final UserManager mUserManager;

    public BlackThemePreferenceController(Context context, String key) {
        super(context, key);
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mUserManager = context.getSystemService(UserManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        try {
            if (mOverlayManager == null) return UNSUPPORTED_ON_DEVICE;
            OverlayInfo info = mOverlayManager.getOverlayInfo(OVERLAY_PACKAGE, UserHandle.myUserId());
            return (info != null) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        } catch (RemoteException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    @Override
    public boolean isChecked() {
        try {
            OverlayInfo info = mOverlayManager.getOverlayInfo(OVERLAY_PACKAGE, UserHandle.myUserId());
            return info != null && info.isEnabled();
        } catch (RemoteException e) {
            Log.w(TAG, "Error reading overlay state", e);
            return false;
        }
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        int parentUserId = UserHandle.myUserId();
        List<UserInfo> profiles = mUserManager.getEnabledProfiles(parentUserId);

        boolean success = true;

        for (UserInfo profile : profiles) {
            int profileId = profile.id;
            try {
                mOverlayManager.setEnabled(OVERLAY_PACKAGE, isChecked, profileId);
                if (isChecked) {
                    mOverlayManager.setHighestPriority(OVERLAY_PACKAGE, profileId);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Error toggling overlay for user " + profileId, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }
}