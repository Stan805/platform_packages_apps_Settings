package com.android.settings.display;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R; // Import for slice menu res if needed

public class BlackThemePreferenceController extends TogglePreferenceController {

    private static final String TAG = "BlackThemePrefCtrl";
    // This matches your RRO Manifest package name exactly
    private static final String OVERLAY_PACKAGE = "com.android.overlay.customization.blacktheme";

    private final IOverlayManager mOverlayManager;

    public BlackThemePreferenceController(Context context, String key) {
        super(context, key);
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    @Override
    public int getAvailabilityStatus() {
        // Ensures the overlay is installed in the first place
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
        try {
            mOverlayManager.setEnabled(OVERLAY_PACKAGE, isChecked, UserHandle.myUserId());
            if (isChecked) {
                mOverlayManager.setHighestPriority(OVERLAY_PACKAGE, UserHandle.myUserId());
            }
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Error toggling overlay", e);
            return false;
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }
}
