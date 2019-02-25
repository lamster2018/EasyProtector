package com.lahm.easyprotector;

import android.app.Application;

import com.lahm.library.VirtualCheckCallback;
import com.lahm.library.EasyProtectorLib;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.easyprotector
 * Created by lahm on 2018/7/4 16:16 .
 */
public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EasyProtectorLib.checkXposedExistAndDisableIt();
//        可以在启动时创建localServerSocket
//        EasyProtectorLib.checkIsRunningInVirtualApk(getPackageName(), new VirtualCheckCallback() {
//            @Override
//            public void findSuspect() {
//                System.exit(0);
//            }
//        });
    }
}
