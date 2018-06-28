package com.lahm.easyprotector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.lahm.library.EasyProtectorLib;
import com.lahm.library.SecurityCheckUtil;
import com.lahm.library.VirtualApkCheckUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView one = findViewById(R.id.one);
        one.setText(VirtualApkCheckUtil.getSingleInstance().checkByPrivateFilePath(this) ?
                "私有路径检测有多开" : "私有路径检测正常");
        TextView two = findViewById(R.id.two);
        two.setText(VirtualApkCheckUtil.getSingleInstance().checkByOriginApkPackageName(this) ?
                "包名检测有多开" : "包名检测正常");
        TextView three = findViewById(R.id.three);
        three.setText(VirtualApkCheckUtil.getSingleInstance().checkByMultiApkPackageName() ?
                "maps检测有多开" : "maps检测正常");
        TextView four = findViewById(R.id.four);
        four.setText(EasyProtectorLib.checkIsUsingMultiVirtualApp() ?
                "ps检测有多开" : "ps检测正常");

        TextView root = findViewById(R.id.root);
        root.setText(EasyProtectorLib.checkIsRoot() ?
                "有root权限" : "无root权限或root不成功");

        TextView debug = findViewById(R.id.debug);
        debug.setText(SecurityCheckUtil.getSingleInstance().checkIsDebugVersion(this) ?
                "debug-version" : "release-version");

        TextView xp = findViewById(R.id.xp);
        xp.setText(EasyProtectorLib.checkIsXposedExist() ?
                (EasyProtectorLib.checkXposedExistAndDisableIt() ?
                        "有xp框架但关闭成功" : "有xp框架但关闭失败")
                : "无xp框架");

        usb = findViewById(R.id.usb);
        usb.setText(SecurityCheckUtil.getSingleInstance().checkIsDebuggerConnected() ?
                "debugger-connect" : "no-debugger-connect");

        TextView traced = findViewById(R.id.traced);
        traced.setText(EasyProtectorLib.checkIsBeingTracedByJava() ?
                "being traced" : "no-tracer");

        final Button loadSO = findViewById(R.id.loadSO);
        loadSO.setOnClickListener(v -> {
            EasyProtectorLib.checkIsBeingTracedByC();
            loadSO.setText("log中查看traceId");
            loadSO.setClickable(false);
        });

        TextView emulator = findViewById(R.id.emulator);
        emulator.setText(EasyProtectorLib.checkIsRunningInEmulator() ?
                "isEmulator" : "not-emulator");

        Button listen = findViewById(R.id.listen);
        listen.setOnClickListener(v -> listen());
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void listen() {
        VirtualApkCheckUtil.getSingleInstance().checkByPortListening(getPackageName());
    }

    BatteryChangeBroadCastReceiver receiver = new BatteryChangeBroadCastReceiver();
    private TextView usb;

    //最好是开启子线程轮询的方式监听，在插上usb的一瞬间，debugger并没有attach上
    private class BatteryChangeBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_POWER_CONNECTED:
                    usb.setText(
                            SecurityCheckUtil.getSingleInstance().checkIsUsbCharging(MainActivity.this) ?
                                    SecurityCheckUtil.getSingleInstance().checkIsDebuggerConnected() ?
                                            "debugger-connect！！" : "only-usb-charging"
                                    : "only-charging");
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    usb.setText("no-usb-connected");
                    break;
            }
        }
    }
}
