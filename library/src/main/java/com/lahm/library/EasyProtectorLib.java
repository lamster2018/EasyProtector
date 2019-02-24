package com.lahm.library;

import android.content.Context;

import java.net.UnknownHostException;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/5/14 下午9:38 .
 */
public class EasyProtectorLib {

    public static String checkSignature(Context context) {
        return SecurityCheckUtil.getSingleInstance().getSignature(context);
    }

    public static boolean checkIsDebug(Context context) {
        return SecurityCheckUtil.getSingleInstance().checkIsDebugVersion(context) ||
                SecurityCheckUtil.getSingleInstance().checkIsDebuggerConnected();
    }

    public static boolean checkIsPortUsing(String host, int port) {
        try {
            return SecurityCheckUtil.getSingleInstance().isPortUsing(host, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean checkIsRoot() {
        return SecurityCheckUtil.getSingleInstance().isRoot();
    }

    public static boolean checkIsXposedExist() {
        return SecurityCheckUtil.getSingleInstance().isXposedExistByThrow();
    }

    public static boolean checkXposedExistAndDisableIt() {
        return SecurityCheckUtil.getSingleInstance().tryShutdownXposed();
    }

    public static boolean checkHasLoadSO(String soName) {
        return SecurityCheckUtil.getSingleInstance().hasReadProcMaps(soName);
    }

    public static boolean checkIsBeingTracedByJava() {
        return SecurityCheckUtil.getSingleInstance().readProcStatus();
    }

    public static void checkIsBeingTracedByC() {
        NDKUtil.loadLibrariesOnce(null);
//        NDKUtil.loadLibraryByName("antitrace");
    }

    public static boolean checkIsRunningInEmulator(Context context, EmulatorCheckCallback callback) {
        return EmulatorCheckUtil.getSingleInstance().readSysProperty(context, callback);
    }

    public static boolean checkIsRunningInVirtualApk(String uniqueMsg, VirtualCheckCallback callback) {
        return VirtualApkCheckUtil.getSingleInstance().checkByCreateLocalServerSocket(uniqueMsg, callback);
    }
}
