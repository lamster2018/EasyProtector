package com.lahm.library;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/5/14 下午10:31 .
 */
public class SecurityCheckUtil {
    private static volatile SecurityCheckUtil singleInstance;

    private SecurityCheckUtil() {
    }

    public static SecurityCheckUtil getSingleInstance() {
        if (singleInstance == null) {
            synchronized (SecurityCheckUtil.class) {
                if (singleInstance == null) {
                    singleInstance = new SecurityCheckUtil();
                }
            }
        }
        return singleInstance;
    }

    /**
     * 通过包管理器获得指定包名包含签名的包信息
     */
    public String getSignature(Context context) {
        try {
            PackageInfo packageInfo = context.
                    getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_SIGNATURES);
            // 通过返回的包信息获得签名数组
            Signature[] signatures = packageInfo.signatures;
            // 循环遍历签名数组拼接应用签名
            StringBuilder builder = new StringBuilder();
            for (Signature signature : signatures) {
                builder.append(signature.toCharsString());
            }
            // 得到应用签名
            return builder.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 拿appInfo的值去判断
     */
    public boolean checkIsDebugA(Context context) {
        return (context.getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 直接调用debug
     */
    public boolean checkIsDebugB() {
        return android.os.Debug.isDebuggerConnected();
    }

    /**
     * 拿appInfo去找meta值
     */
    public String getApplicationMetaValue(Context context, String name) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        return appInfo.metaData.getString(name);
    }

    /***
     *  true:already in using  false:not using
     */
    public boolean isLoclePortUsing(int port) {
        boolean flag = true;
        try {
            flag = isPortUsing("127.0.0.1", port);
        } catch (Exception e) {
        }
        return flag;
    }

    /***
     *  true:already in using  false:not using
     * @throws UnknownHostException
     */
    public boolean isPortUsing(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {
        }
        return flag;
    }

    /**
     * 查root
     * https://mp.weixin.qq.com/s/Je1kRksxHTTYb4l9x3bTmQ
     * 检查rom编译版本
     * https://www.jianshu.com/p/7407cf6c34bd
     * 检查su文件
     * https://www.jianshu.com/p/f9f39704e30c
     */
    public boolean isRoot() {
        int debugProp = getroDebugProp();
        if (debugProp == 0)//user版本，继续查su文件
            return isSUExist();
        int secureProp = getroSecureProp();
        if (secureProp == 0)//eng版本，自带root权限
            return true;
        else return isSUExist();//userdebug版本，继续查su文件
    }

    private int getroSecureProp() {
        int secureProp;
        Object roSecureObj;
        try {
            roSecureObj = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class)
                    .invoke(null, "ro.secure");

        } catch (Throwable fuck) {
            roSecureObj = null;
        }
        if (roSecureObj == null) secureProp = 0;
        else {
            if ("0".equals(roSecureObj)) secureProp = 0;
            else secureProp = 1;
        }
        return secureProp;
    }

    private int getroDebugProp() {
        int debugProp;
        Object roDebugObj;
        try {
            roDebugObj = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class)
                    .invoke(null, "ro.debuggable");

        } catch (Throwable fuck) {
            roDebugObj = null;
        }
        if (roDebugObj == null) debugProp = 0;
        else {
            if ("0".equals(roDebugObj)) debugProp = 0;
            else debugProp = 1;
        }
        return debugProp;
    }

    private boolean isSUExist() {
        File file = null;
        String[] paths = {"/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"};
        for (String path : paths) {
            file = new File(path);
            if (file.exists()) return true;
        }
        return false;
    }

    private static final String XPOSED_HELPERS = "de.robv.android.xposed.XposedHelpers";
    private static final String XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge";

    /**
     * 检查xposed是否存在，就是查文件
     */
    public boolean isXposedExists() {
        try {
            Object xpHelperObj = ClassLoader.getSystemClassLoader().loadClass(XPOSED_HELPERS).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return true;
        } catch (IllegalAccessException e) {
            //实测debug跑到这里报异常
            e.printStackTrace();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            Object xpBridgeObj = ClassLoader.getSystemClassLoader().loadClass(XPOSED_BRIDGE).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return true;
        } catch (IllegalAccessException e) {
            //实测debug跑到这里报异常
            e.printStackTrace();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 尝试关闭xp的全局开关
     *
     * @return
     */
    public boolean tryShutdownXposed() {
        if (isXposedExists()) {
            Field xpdisabledHooks = null;
            try {
                xpdisabledHooks = ClassLoader.getSystemClassLoader()
                        .loadClass("de.robv.android.xposed.XposedBridge")
                        .getDeclaredField("disableHooks");
                xpdisabledHooks.setAccessible(true);
                xpdisabledHooks.set(null, Boolean.TRUE);
                return true;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        } else return true;
    }

    /**
     * 读取已经load的so库
     */
    public boolean hasReadProcMaps(String paramString) {
        try {
            Object localObject = new HashSet();
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/maps"));
            for (; ; ) {
                String str = localBufferedReader.readLine();
                if (str == null) {
                    break;
                }
                if ((str.endsWith(".so")) || (str.endsWith(".jar"))) {
                    ((Set) localObject).add(str.substring(str.lastIndexOf(" ") + 1));
                }
            }
            localBufferedReader.close();
            localObject = ((Set) localObject).iterator();
            while (((Iterator) localObject).hasNext()) {
                boolean bool = ((String) ((Iterator) localObject).next()).contains(paramString);
                if (bool) {
                    return true;
                }
            }
        } catch (Exception fuck) {
        }
        return false;
    }

    /**
     * 拿status里的traceid，轮询实际上也是读取这个文件
     */
    public boolean readProcStatus() {
        try {
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/status"));
            String tracerPid = "";
            for (; ; ) {
                String str = localBufferedReader.readLine();
                if (str.contains("TracerPid")) {
                    tracerPid = str.substring(str.indexOf(":") + 1, str.length()).trim();
                    break;
                }
                if (str == null) {
                    break;
                }
            }
            localBufferedReader.close();
            if ("0".equals(tracerPid)) return false;
            else return true;
        } catch (Exception fuck) {
            return false;
        }
    }
}
