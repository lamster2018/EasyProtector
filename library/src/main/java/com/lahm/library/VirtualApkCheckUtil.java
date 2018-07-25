package com.lahm.library;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Project Name:checkMultiApk
 * Package Name:com.lahm.library
 * Created by lahm on 2018/5/14 下午4:11
 */
public class VirtualApkCheckUtil {
    private String TAG = "test";
    private static volatile VirtualApkCheckUtil singleInstance;

    private VirtualApkCheckUtil() {
    }

    public static VirtualApkCheckUtil getSingleInstance() {
        if (singleInstance == null) {
            synchronized (VirtualApkCheckUtil.class) {
                if (singleInstance == null) {
                    singleInstance = new VirtualApkCheckUtil();
                }
            }
        }
        return singleInstance;
    }

    public boolean checkByPrivateFilePath(Context context) {
        String path = context.getFilesDir().getPath();
        for (String virtualPkg : virtualPkgs) {
            if (path.contains(virtualPkg)) return true;
        }
        return false;
    }

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
            "com.lody.virtual",//VirtualXposed，VirtualApp
            "com.qihoo.magic"//360分身大师
    };

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

    public boolean checkByHasSameUid() {
        String filter = getUidStrFormat();
        if (TextUtils.isEmpty(filter)) return false;

        String result = CommandUtil.getSingleInstance().exec("ps");
        if (TextUtils.isEmpty(result)) return false;

        String[] lines = result.split("\n");
        if (lines == null || lines.length <= 0) return false;

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

    private String getUidStrFormat() {
        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
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

    public void checkByPortListening(String secret, VirtualCheckCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("you have to set a callback to deal with suspect");
        this.checkCallback = callback;
        startClient(secret);
        new ServerThread(secret).start();
    }

    private class ServerThread extends Thread {
        String secret;

        private ServerThread(String secret) {
            this.secret = secret;
        }

        @Override
        public void run() {
            super.run();
            startServer(secret);
        }
    }

    private void startServer(String secret) {
        Random random = new Random();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1",
                    random.nextInt(55534) + 10000));
            while (true) {
                Socket socket = serverSocket.accept();
                ReadThread readThread = new ReadThread(secret, socket);
                readThread.start();
//                serverSocket.close();
            }
        } catch (BindException e) {
            startServer(secret);//may be loop forever
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VirtualCheckCallback checkCallback;

    private class ReadThread extends Thread {
        private ReadThread(String secret, Socket socket) {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                byte buffer[] = new byte[1024 * 4];
                int temp = 0;
                while ((temp = inputStream.read(buffer)) != -1) {
                    String result = new String(buffer, 0, temp);
                    if (result.contains(secret)) {
                        checkCallback.findSuspect();
                        checkCallback = null;//当检测到同时有两个的时候，处理完回调后把callback置空
                    }
                }
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startClient(String secret) {
        String tcp6 = CommandUtil.getSingleInstance().exec("cat /proc/net/tcp6");
        if (TextUtils.isEmpty(tcp6)) return;
        String[] lines = tcp6.split("\n");
        ArrayList<Integer> portList = new ArrayList<>();
        for (int i = 0, len = lines.length; i < len; i++) {
            int localHost = lines[i].indexOf("0100007F:");//127.0.0.1:
            if (localHost < 0) continue;
            String singlePort = lines[i].substring(localHost + 9, localHost + 13);
            Integer port = Integer.parseInt(singlePort, 16);
            portList.add(port);
        }
        if (portList.isEmpty()) return;
        for (int port : portList) {
            new ClientThread(secret, port).start();
        }
    }

    private class ClientThread extends Thread {
        String secret;
        int port;

        private ClientThread(String secret, int port) {
            this.secret = secret;
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                Socket socket = new Socket("127.0.0.1", port);
                socket.setSoTimeout(2000);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write((secret + "\n").getBytes("utf-8"));
                outputStream.flush();
                socket.shutdownOutput();

                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String info = null;
                while ((info = bufferedReader.readLine()) != null) {
                    Log.i(TAG, "ClientThread: " + info);
                }

                bufferedReader.close();
                inputStream.close();
                socket.close();
            } catch (ConnectException e) {
                Log.i(TAG, port + "port refused");
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
