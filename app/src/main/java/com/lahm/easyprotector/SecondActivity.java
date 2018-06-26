package com.lahm.easyprotector;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.lahm.library.CommandUtil;

import java.io.BufferedReader;
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
import java.util.Random;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.easyprotector
 * Created by lahm on 2018/6/26 15:36 .
 */
public class SecondActivity extends AppCompatActivity {
    private final String TAG = "ceshi";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.client).setOnClickListener(v -> client());
        findViewById(R.id.server).setOnClickListener(v -> server());
    }

    private void server() {
        new Thread(runnable).start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startServer();
        }
    };

    private void startServer() {
        Random random = new Random();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", random.nextInt(55534) + 10000));
            while (true) {
                Log.i(TAG, "startServer: 等等待");
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
//                serverSocket.close();
            }
        } catch (BindException e) {
            startServer();//may be loop forever
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerThread extends Thread {
        private ServerThread(Socket socket) {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                byte buffer[] = new byte[1024 * 4];
                int temp = 0;
                // 从InputStream当中读取客户端所发送的数据
                while ((temp = inputStream.read(buffer)) != -1) {
                    String result = new String(buffer, 0, temp);
                    if (result.contains(getPackageName())) {
//                        System.exit(0);
                        textView.setText("sdasdasd");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    TextView textView;

    private void client() {
//        System.out.println("ceshi" + CommandUtil.getSingleInstance().exec("netstat -nltp"));
//        System.out.println("ceshi" + CommandUtil.getSingleInstance().exec("busybox netstat -nltp"));
//        System.out.println("ceshi" + CommandUtil.getSingleInstance().exec("cat /proc/net/tcp6"));
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
            new ClientThread(port).start();
        }
    }

    private class ClientThread extends Thread {
        int port;

        private ClientThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                Socket socket = new Socket("127.0.0.1", port);
                socket.setSoTimeout(2000);
                Log.i(TAG, "成功" + port);
                OutputStream outputStream = socket.getOutputStream();
                // 写入需要发送的数据到输出流对象中
                outputStream.write((getPackageName() + "\n").getBytes("utf-8"));
                // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                // 发送数据到服务端
                outputStream.flush();
                // 关闭输出流
                socket.shutdownOutput();
                //获取输入流，读取服务器端的响应信息
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String info = null;
                while ((info = bufferedReader.readLine()) != null) {
//                    if(getPackageName().equals(info))System.exit(0);
                    Log.i(TAG, "ClientThread: " + info);
                }
                //关闭资源
                bufferedReader.close();
                inputStream.close();
                socket.close();

            } catch (ConnectException e) {
                Log.i(TAG, port + "端口拒绝");
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
