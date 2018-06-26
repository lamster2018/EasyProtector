package com.lahm.easyprotector;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.easyprotector
 * Created by lahm on 2018/6/12 14:09 .
 */
public class TestAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://  //切换页面的时候触发
                break;
        }
        if (event.getPackageName().toString().equals("com.android.packageinstaller")) {
            installAPK(event);
        }

    }

    @Override
    public void onInterrupt() {

    }

    private void installAPK(AccessibilityEvent event) {
        AccessibilityNodeInfo source = getRootInActiveWindow();
        List<AccessibilityNodeInfo> nextInfos = source.findAccessibilityNodeInfosByText("下一步");
        nextClick(nextInfos);
        List<AccessibilityNodeInfo> installInfos = source.findAccessibilityNodeInfosByText("安装");
        nextClick(installInfos);
        List<AccessibilityNodeInfo> openInfos = source.findAccessibilityNodeInfosByText("打开");
        nextClick(openInfos);
    }

    private void nextClick(List<AccessibilityNodeInfo> infos) {
        if (infos != null)
            for (AccessibilityNodeInfo info : infos) {
                if (info.isEnabled() && info.isClickable())
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
    }

}
