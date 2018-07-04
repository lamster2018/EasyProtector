package com.lahm.easyprotector;

import android.app.Application;

import com.lahm.library.VirtualApkCheckUtil;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.easyprotector
 * Created by lahm on 2018/7/4 16:16 .
 */
public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VirtualApkCheckUtil.getSingleInstance().checkByPortListening(getPackageName());
    }
}
