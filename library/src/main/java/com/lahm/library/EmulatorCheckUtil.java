package com.lahm.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.text.TextUtils;

import static android.content.Context.SENSOR_SERVICE;
import static com.lahm.library.CheckResult.RESULT_EMULATOR;
import static com.lahm.library.CheckResult.RESULT_MAYBE_EMULATOR;
import static com.lahm.library.CheckResult.RESULT_UNKNOWN;

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

    public boolean readSysProperty(Context context, EmulatorCheckCallback callback) {
        if (context == null)
            throw new IllegalArgumentException("context must not be null");

        int suspectCount = 0;

        //检测硬件名称
        CheckResult hardwareResult = checkFeaturesByHardware();
        switch (hardwareResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("hardware = " + hardwareResult.value);
                return true;
        }

        //检测渠道
        CheckResult flavorResult = checkFeaturesByFlavor();
        switch (flavorResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("flavor = " + flavorResult.value);
                return true;
        }

        //检测设备型号
        CheckResult modelResult = checkFeaturesByModel();
        switch (modelResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("model = " + modelResult.value);
                return true;
        }

        //检测硬件制造商
        CheckResult manufacturerResult = checkFeaturesByManufacturer();
        switch (manufacturerResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null)
                    callback.findEmulator("manufacturer = " + manufacturerResult.value);
                return true;
        }

        //检测主板名称
        CheckResult boardResult = checkFeaturesByBoard();
        switch (boardResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("board = " + boardResult.value);
                return true;
        }

        //检测主板平台
        CheckResult platformResult = checkFeaturesByPlatform();
        switch (platformResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("platform = " + platformResult.value);
                return true;
        }

        //检测基带信息
        CheckResult baseBandResult = checkFeaturesByBaseBand();
        switch (baseBandResult.result) {
            case RESULT_MAYBE_EMULATOR:
                suspectCount += 2;//模拟器基带信息为null的情况概率相当大
                break;
            case RESULT_EMULATOR:
                if (callback != null) callback.findEmulator("baseBand = " + baseBandResult.value);
                return true;
        }

        //检测传感器数量
        int sensorNumber = getSensorNumber(context);
        if (sensorNumber <= 7) ++suspectCount;

        //检测已安装第三方应用数量
        int userAppNumber = getUserAppNumber();
        if (userAppNumber <= 5) ++suspectCount;

        //检测是否支持闪光灯
        boolean supportCameraFlash = supportCameraFlash(context);
        if (!supportCameraFlash) ++suspectCount;
        //检测是否支持相机
        boolean supportCamera = supportCamera(context);
        if (!supportCamera) ++suspectCount;
        //检测是否支持蓝牙
        boolean supportBluetooth = supportBluetooth(context);
        if (!supportBluetooth) ++suspectCount;

        //检测光线传感器
        boolean hasLightSensor = hasLightSensor(context);
        if (!hasLightSensor) ++suspectCount;

        //检测进程组信息
        CheckResult cgroupResult = checkFeaturesByCgroup();
        if (cgroupResult.result == RESULT_MAYBE_EMULATOR) ++suspectCount;

        if (callback != null) {
            StringBuffer stringBuffer = new StringBuffer("Test start")
                    .append("\r\n").append("hardware = ").append(hardwareResult.value)
                    .append("\r\n").append("flavor = ").append(flavorResult.value)
                    .append("\r\n").append("model = ").append(modelResult.value)
                    .append("\r\n").append("manufacturer = ").append(manufacturerResult.value)
                    .append("\r\n").append("board = ").append(boardResult.value)
                    .append("\r\n").append("platform = ").append(platformResult.value)
                    .append("\r\n").append("baseBand = ").append(baseBandResult.value)
                    .append("\r\n").append("sensorNumber = ").append(sensorNumber)
                    .append("\r\n").append("userAppNumber = ").append(userAppNumber)
                    .append("\r\n").append("supportCamera = ").append(supportCamera)
                    .append("\r\n").append("supportCameraFlash = ").append(supportCameraFlash)
                    .append("\r\n").append("supportBluetooth = ").append(supportBluetooth)
                    .append("\r\n").append("hasLightSensor = ").append(hasLightSensor)
                    .append("\r\n").append("cgroupResult = ").append(cgroupResult.value)
                    .append("\r\n").append("suspectCount = ").append(suspectCount);
            callback.findEmulator(stringBuffer.toString());
        }
        //嫌疑值大于3，认为是模拟器
        return suspectCount > 3;
    }

    private int getUserAppNum(String userApps) {
        if (TextUtils.isEmpty(userApps)) return 0;
        String[] result = userApps.split("package:");
        return result.length;
    }

    private String getProperty(String propName) {
        String property = CommandUtil.getSingleInstance().getProperty(propName);
        return TextUtils.isEmpty(property) ? null : property;
    }

    /**
     * 特征参数-硬件名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByHardware() {
        String hardware = getProperty("ro.hardware");
        if (null == hardware) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = hardware.toLowerCase();
        switch (tempValue) {
            case "ttvm"://天天模拟器
            case "nox"://夜神模拟器
            case "cancro"://网易MUMU模拟器
            case "intel"://逍遥模拟器
            case "vbox":
            case "vbox86"://腾讯手游助手
            case "android_x86"://雷电模拟器
                result = RESULT_EMULATOR;
                break;
            default:
                result = RESULT_UNKNOWN;
                break;
        }
        return new CheckResult(result, hardware);
    }

    /**
     * 特征参数-渠道
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByFlavor() {
        String flavor = getProperty("ro.build.flavor");
        if (null == flavor) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = flavor.toLowerCase();
        if (tempValue.contains("vbox")) result = RESULT_EMULATOR;
        else if (tempValue.contains("sdk_gphone")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, flavor);
    }

    /**
     * 特征参数-设备型号
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByModel() {
        String model = getProperty("ro.product.model");
        if (null == model) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = model.toLowerCase();
        if (tempValue.contains("google_sdk")) result = RESULT_EMULATOR;
        else if (tempValue.contains("emulator")) result = RESULT_EMULATOR;
        else if (tempValue.contains("android sdk built for x86")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, model);
    }

    /**
     * 特征参数-硬件制造商
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByManufacturer() {
        String manufacturer = getProperty("ro.product.manufacturer");
        if (null == manufacturer) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = manufacturer.toLowerCase();
        if (tempValue.contains("genymotion")) result = RESULT_EMULATOR;
        else if (tempValue.contains("netease")) result = RESULT_EMULATOR;//网易MUMU模拟器
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, manufacturer);
    }

    /**
     * 特征参数-主板名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByBoard() {
        String board = getProperty("ro.product.board");
        if (null == board) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = board.toLowerCase();
        if (tempValue.contains("android")) result = RESULT_EMULATOR;
        else if (tempValue.contains("goldfish")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, board);
    }

    /**
     * 特征参数-主板平台
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByPlatform() {
        String platform = getProperty("ro.board.platform");
        if (null == platform) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = platform.toLowerCase();
        if (tempValue.contains("android")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, platform);
    }

    /**
     * 特征参数-基带信息
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private CheckResult checkFeaturesByBaseBand() {
        String baseBandVersion = getProperty("gsm.version.baseband");
        if (null == baseBandVersion) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        if (baseBandVersion.contains("1.0.0.0")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new CheckResult(result, baseBandVersion);
    }

    /**
     * 获取传感器数量
     */
    private int getSensorNumber(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return sm.getSensorList(Sensor.TYPE_ALL).size();
    }

    /**
     * 获取已安装第三方应用数量
     */
    private int getUserAppNumber() {
        String userApps = CommandUtil.getSingleInstance().exec("pm list package -3");
        return getUserAppNum(userApps);
    }

    /**
     * 是否支持相机
     */
    private boolean supportCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 是否支持闪光灯
     */
    private boolean supportCameraFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * 是否支持蓝牙
     */
    private boolean supportBluetooth(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    /**
     * 判断是否存在光传感器来判断是否为模拟器
     * 部分真机也不存在温度和压力传感器。其余传感器模拟器也存在。
     *
     * @return false为模拟器
     */
    private boolean hasLightSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光线传感器
        if (null == sensor) return false;
        else return true;
    }

    /**
     * 特征参数-进程组信息
     */
    private CheckResult checkFeaturesByCgroup() {
        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        if (null == filter) return new CheckResult(RESULT_MAYBE_EMULATOR, null);
        return new CheckResult(RESULT_UNKNOWN, filter);
    }
}
