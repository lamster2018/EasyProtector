package com.lahm.library;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextUtils;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/6/8 15:01 .
 */
public class EmulatorCheckUtil {
    private EmulatorCheckUtil() {

    }

    private static class SingletonHolder {
        private static final EmulatorCheckUtil INSTANCE = new EmulatorCheckUtil();
    }

    public static final EmulatorCheckUtil getSingleInstance() {
        return SingletonHolder.INSTANCE;
    }

    //逍遥安卓模拟器能模拟cpu信息
    public String readCpuInfo() {
        String result = CommandUtil.getSingleInstance().exec("cat /proc/cpuinfo");
        return result;
    }

    //逍遥安卓模拟器读取不到该文件
    public boolean readUidInfo() {
        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        return filter == null || filter.length() == 0;
    }

    public boolean readSysProperty() {
        int suspectCount = 0;

        String baseBandVersion = CommandUtil.getSingleInstance().getProperty("gsm.version.baseband");
        if (TextUtils.isEmpty(baseBandVersion)) ++suspectCount;

        String buildFlavor = CommandUtil.getSingleInstance().getProperty("ro.build.flavor");
        if (TextUtils.isEmpty(buildFlavor) | (buildFlavor != null && buildFlavor.contains("vbox")))
            ++suspectCount;

        String productBoard = CommandUtil.getSingleInstance().getProperty("ro.product.board");
        if (TextUtils.isEmpty(productBoard) | (productBoard != null && productBoard.contains("android")))
            ++suspectCount;

        String boardPlatform = CommandUtil.getSingleInstance().getProperty("ro.board.platform");
        if (TextUtils.isEmpty(boardPlatform) | (boardPlatform != null && boardPlatform.contains("android")))
            ++suspectCount;

        if (!TextUtils.isEmpty(productBoard)
                && !TextUtils.isEmpty(boardPlatform)
                && !productBoard.equals(boardPlatform))
            ++suspectCount;

        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        if (TextUtils.isEmpty(filter)) ++suspectCount;

        return suspectCount > 2;
    }

    public String printSysProperty() {
        String baseBandVersion = CommandUtil.getSingleInstance().getProperty("gsm.version.baseband");
        String buildFlavor = CommandUtil.getSingleInstance().getProperty("ro.build.flavor");
        String productBoard = CommandUtil.getSingleInstance().getProperty("ro.product.board");
        String boardPlatform = CommandUtil.getSingleInstance().getProperty("ro.board.platform");
        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        StringBuffer stringBuffer = new StringBuffer("ceshi start|")
                .append(baseBandVersion)
                .append("|")
                .append(buildFlavor)
                .append("|")
                .append(productBoard)
                .append("|")
                .append(boardPlatform)
                .append("|")
                .append(filter)
                .append("|end");
        return stringBuffer.toString();
    }

    public boolean hasGyroscopeSensor(Context context) {
        return getSystemSensor(context, Sensor.TYPE_GYROSCOPE);
    }

    public boolean hasLightSensor(Context context) {
        return getSystemSensor(context, Sensor.TYPE_LIGHT);
    }

    private boolean getSystemSensor(Context context, int type) {
        if (context == null) return false;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager == null) return false;
        Sensor sensor = manager.getDefaultSensor(type);
        if (sensor == null) return false;
        manager.registerListener(new MySensorEventListener(manager), sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }

    private class MySensorEventListener implements SensorEventListener {
        SensorManager sensorManager;

        MySensorEventListener(SensorManager sensorManager) {
            this.sensorManager = sensorManager;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    @Deprecated
    public String readBuildInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("-\n")
                .append("BOARD-")
                .append(android.os.Build.BOARD)
                .append("\nBOOTLOADER-")
                .append(android.os.Build.BOOTLOADER)
                .append("\nBRAND-")
                .append(android.os.Build.BRAND)
                .append("\nDEVICE-")
                .append(android.os.Build.DEVICE)
                .append("\nHARDWARE-")
                .append(android.os.Build.HARDWARE)
                .append("\nMODEL-")
                .append(android.os.Build.MODEL)
                .append("\nPRODUCT-")
                .append(android.os.Build.PRODUCT);
        return sb.toString();
    }
}
