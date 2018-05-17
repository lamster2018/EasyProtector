package com.lahm.library;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Project Name:checkMultiApk
 * Package Name:com.lahm.library
 * Created by lahm on 2018/5/14 下午4:11
 * <p>
 * 检测多开app方案
 * 方案均来自
 * 1.https://blog.darkness463.top/2018/05/04/Android-Virtual-Check/
 * 2.https://www.jianshu.com/p/216d65d9971e
 * <p>
 * 测试机器
 * 红米3S Android6.0 eng rom--ok
 * 华为P9 Android7.0 EMUI5.0--多开分身6.9版本失败
 * 小米MIX2 Android8.0 MIUI稳定版9.5--多开分身6.9版本失败
 * 一加5T Android8.1 氢OS--多开分身6.9版本失败
 */
public class CheckMultiUtil {

    private static volatile CheckMultiUtil singleInstance;

    private CheckMultiUtil() {
    }

    public static CheckMultiUtil getSingleInstance() {
        if (singleInstance == null) {
            synchronized (CheckMultiUtil.class) {
                if (singleInstance == null) {
                    singleInstance = new CheckMultiUtil();
                }
            }
        }
        return singleInstance;
    }

    /**
     * 应用列表检测
     * 多开App都会对context.getPackageName()进行处理，让这个方法返回原始App的包名，
     * 因此在被多开的App看来，多开App的包名和原始的App的包名一样，
     * 多开环境下遍历应用列表时会发现包名等于原始App的包名的应用会有两个
     * <p>
     * 翻译一下，多开app把原始app克隆了，并让自己的包名跟原始app一样，
     * 当使用克隆app时，会检测到原始app的包名会和多开app包名一样（就是有两个一样的包名）
     *
     * @param context context
     * @return
     */
    public boolean checkByOriginApkPackageName(Context context) {
        try {
            if (context == null) {
                return false;
            }
            int count = 0;
            String packageName = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> pkgs = pm.getInstalledPackages(0);
            for (PackageInfo info : pkgs) {
                if (packageName.equals(info.packageName)) {
                    count++;
                }
            }
            return count > 1;
        } catch (Exception ignore) {
        }
        return false;
    }

    // 多开App包名列表
    private String[] virtualPkgs = {
            "com.bly.dkplat",//多开分身
            "com.lbe.parallel",//平行空间
            "com.excelliance.dualaid",//双开助手
            "com.qihoo.magic"//360分身大师
    };

    /**
     * maps检测
     * 因为多开App会加载一些自己的so到内存空间
     * 通过读取/proc/self/maps
     * 如果maps中有多开App的包名的东西,则认为是多开
     * 需要维护一份多开app的包名集合
     *
     * @return
     */
    public boolean checkByMultiApkPackageName() {
        BufferedReader bufr = null;
        try {
            bufr = new BufferedReader(new FileReader("/proc/self/maps"));
            String line;
            while ((line = bufr.readLine()) != null) {
                for (String pkg : virtualPkgs) {
                    if (line.contains(pkg)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignore) {

        } finally {
            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException e) {

                }
            }
        }
        return false;
    }

    /**
     * ps检测
     * 如果满足同一uid下的两个进程对应的包名，在"/data/data"下有两个私有目录，则该应用被多开
     *
     * @return
     */
    public boolean checkByHasSameUid() {
        String filter = getUidStrFormat();

        String result = exec("ps");
        if (result == null || result.isEmpty()) {
            return false;
        }

        String[] lines = result.split("\n");
        if (lines == null || lines.length <= 0) {
            return false;
        }

        int exitDirCount = 0;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(filter)) {
                int pkgStartIndex = lines[i].lastIndexOf(" ");
                String processName = lines[i].substring(pkgStartIndex <= 0
                        ? 0 : pkgStartIndex + 1, lines[i].length());
                File dataFile = new File(String.format("/data/data/%s", processName, Locale.CHINA));
                if (dataFile.exists()) {
                    exitDirCount++;
                }
            }
        }

        return exitDirCount > 1;
    }


    private String exec(String command) {
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

            bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedOutputStream.write(command.getBytes());
            bufferedOutputStream.write('\n');
            bufferedOutputStream.flush();
            bufferedOutputStream.close();

            process.waitFor();

            String outputStr = getStrFromBufferInputSteam(bufferedInputStream);
            return outputStr;
        } catch (Exception e) {
            return null;
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private String getStrFromBufferInputSteam(BufferedInputStream bufferedInputStream) {
        if (null == bufferedInputStream) {
            return "";
        }
        int BUFFER_SIZE = 512;
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder result = new StringBuilder();
        try {
            while (true) {
                int read = bufferedInputStream.read(buffer);
                if (read > 0) {
                    result.append(new String(buffer, 0, read));
                }
                if (read < BUFFER_SIZE) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private String getUidStrFormat() {
        String filter = exec("cat /proc/self/cgroup");
        if (filter == null || filter.length() == 0) {
            return null;
        }

        int uidStartIndex = filter.lastIndexOf("uid");
        int uidEndIndex = filter.lastIndexOf("/pid");
        if (uidStartIndex < 0) {
            return null;
        }
        if (uidEndIndex <= 0) {
            uidEndIndex = filter.length();
        }

        filter = filter.substring(uidStartIndex + 4, uidEndIndex);
        try {
            String strUid = filter.replaceAll("\n", "");
            if (isNumber(strUid)) {
                int uid = Integer.valueOf(strUid);
                filter = String.format("u0_a%d", uid - 10000);
                return filter;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isNumber(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
