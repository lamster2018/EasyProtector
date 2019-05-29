package com.lahm.library;

import android.content.Context;
import android.hardware.Sensor;
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

    public boolean readSysProperty(Context context, EmulatorCheckCallback callback) {
        if (context == null)
            throw new IllegalArgumentException("context must not be null");

        int suspectCount = 0;

        String baseBandVersion = getProperty("gsm.version.baseband");
        if (null == baseBandVersion || baseBandVersion.contains("1.0.0.0"))
            suspectCount += 2;// 提高了权重，因为已有模拟器都为null （夜神，雷电，腾讯手游，mumu，逍遥） - 2019.5.29

        String buildFlavor = getProperty("ro.build.flavor");
        if (null == buildFlavor || buildFlavor.contains("vbox") || buildFlavor.contains("sdk_gphone"))
            ++suspectCount;

        String productBoard = getProperty("ro.product.board");
        if (null == productBoard || productBoard.contains("android") | productBoard.contains("goldfish"))
            ++suspectCount;

        String boardPlatform = getProperty("ro.board.platform");
        if (null == boardPlatform || boardPlatform.contains("android"))
            ++suspectCount;

        String hardWare = getProperty("ro.hardware");
        if (null == hardWare) ++suspectCount;
        else if (hardWare.toLowerCase().contains("ttvm")) suspectCount += 10;
        else if (hardWare.toLowerCase().contains("nox")) suspectCount += 10;
		
		String genymotion_version = getProperty("ro.genymotion.version");
        boolean genymotion_versionIllegal = null != genymotion_version || Build.MANUFACTURER.contains("Genymotion");
        if (genymotion_versionIllegal) suspectCount++;// 检验genymotion
		
		String vbox_dpi = getProperty("androVM.vbox_dpi");
        boolean vbox_dpiIllegal = null != vbox_dpi || (null != buildFlavor && buildFlavor.contains("vbox")) || Build.PRODUCT.contains("vbox86p");
        if (vbox_dpiIllegal) suspectCount++;// 检验vbox

		
		String fake_camera = getProperty("qemu.sf.fake_camera");
        boolean fake_cameraIllegal = null != fake_camera;
        if (fake_cameraIllegal) suspectCount++;// 检验相机
		
		boolean productModel = Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86");
        if (productModel) suspectCount++;// 检验原生虚拟机
		
		boolean productDevice = Build.DEVICE.startsWith("generic") || Build.DEVICE.contains("x86");
        if (productDevice) suspectCount++;// 检验原生虚拟机
		
		boolean productName = Build.PRODUCT.contains("emulator") || Build.PRODUCT.contains("x86");
        if (productName) suspectCount++;// 检验原生虚拟机
		
        String cameraFlash;
        String sensorNum = "sensorNum";
        boolean isSupportCameraFlash = context.getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        if (!isSupportCameraFlash) ++suspectCount;
        cameraFlash = isSupportCameraFlash ? "support CameraFlash" : "unsupport CameraFlash";

        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        int sensorSize = sm.getSensorList(Sensor.TYPE_ALL).size();
        if (sensorSize < 7) ++suspectCount;
        sensorNum = sensorNum + sensorSize;


        String userApps = CommandUtil.getSingleInstance().exec("pm list package -3");
        String userAppNum = "userAppNum";
        int userAppSize = getUserAppNum(userApps);
        if (userAppSize < 5) ++suspectCount;
        userAppNum = userAppNum + userAppSize;

        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        if (null == filter) ++suspectCount;

        if (callback != null) {
            StringBuffer stringBuffer = new StringBuffer("ceshi start|")
                    .append(baseBandVersion).append("|")
                    .append(buildFlavor).append("|")
                    .append(productBoard).append("|")
                    .append(boardPlatform).append("|")
                    .append(hardWare).append("|")
                    .append(cameraFlash).append("|")
                    .append(sensorNum).append("|")
                    .append(userAppNum).append("|")
                    .append(filter).append("|end");
            callback.findEmulator(stringBuffer.toString());
        }

        return suspectCount >= 4;
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
}
