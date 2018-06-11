package com.lahm.library;

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

        String basebandVersion = CommandUtil.getSingleInstance().getProperty("gsm.version.baseband");
        if (basebandVersion == null | "".equals(basebandVersion)) ++suspectCount;

        String buildFlavor = CommandUtil.getSingleInstance().getProperty("ro.build.flavor");
        if (buildFlavor == null | "".equals(buildFlavor) | (buildFlavor != null && buildFlavor.contains("vbox")))
            ++suspectCount;

        String productBoard = CommandUtil.getSingleInstance().getProperty("ro.product.board");
        if (productBoard == null | "".equals(productBoard)) ++suspectCount;

        String boardPlatform = CommandUtil.getSingleInstance().getProperty("ro.board.platform");
        if (boardPlatform == null | "".equals(boardPlatform)) ++suspectCount;

        if (productBoard != null && boardPlatform != null && !productBoard.equals(boardPlatform))
            ++suspectCount;

        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        if (filter == null || filter.length() == 0) ++suspectCount;

        return suspectCount > 2;
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
