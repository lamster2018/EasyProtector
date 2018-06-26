package com.lahm.library;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/6/13 9:47 .
 */
public class AccessibilityServicesCheckUtil {
    private AccessibilityServicesCheckUtil() {
    }

    private static class SingletonHolder {
        private static AccessibilityServicesCheckUtil INSTANCE = new AccessibilityServicesCheckUtil();
    }

    public static final AccessibilityServicesCheckUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Intent go2SetAccessibilityService() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        return intent;
    }

    public boolean checkAccessibilityEnabled(Context context, String packageName) {
        int hasSetSetting;
        try {
            hasSetSetting = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
            if (hasSetSetting == 1) {
                String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    simpleStringSplitter.setString(settingValue);
                    while (simpleStringSplitter.hasNext()) {
                        String accessibilityService = simpleStringSplitter.next();
                        if (accessibilityService.contains(packageName)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Settings.SettingNotFoundException e) {

        }
        return false;
    }
}
