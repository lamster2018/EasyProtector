package com.lahm.easyprotector;

import android.app.Application;

import com.lahm.library.EasyProtectorLib;
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
        //AS模拟器的端口检测法会有问题，会抢localhost，所以这里加了个判断，平时真机可以直接调用端口检测
        if (!EasyProtectorLib.checkIsRunningInEmulator())
            VirtualApkCheckUtil.getSingleInstance().checkByPortListening(getPackageName());
    }
}
