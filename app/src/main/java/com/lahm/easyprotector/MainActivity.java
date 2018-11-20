package com.lahm.easyprotector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lahm.library.CommandUtil;
import com.lahm.library.EasyProtectorLib;
import com.lahm.library.EmulatorCheckCallback;
import com.lahm.library.EmulatorCheckUtil;
import com.lahm.library.SecurityCheckUtil;
import com.lahm.library.VirtualApkCheckUtil;

public class MainActivity extends AppCompatActivity {
    EditText input;
    Button output, clickEmulatorDetect;
    TextView emulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView one = findViewById(R.id.one);
        one.setText(VirtualApkCheckUtil.getSingleInstance().checkByPrivateFilePath(this) ?
                "privatePath-NO" : "privatePath-OK");
        TextView two = findViewById(R.id.two);
        two.setText(VirtualApkCheckUtil.getSingleInstance().checkByOriginApkPackageName(this) ?
                "packageName-NO" : "packageName-OK");
        TextView three = findViewById(R.id.three);
        three.setText(VirtualApkCheckUtil.getSingleInstance().checkByMultiApkPackageName() ?
                "maps-NO" : "maps-OK");
        TextView four = findViewById(R.id.four);
        four.setText(EasyProtectorLib.checkIsUsingMultiVirtualApp() ?
                "process-NO" : "process-OK");

        TextView root = findViewById(R.id.root);
        root.setText(EasyProtectorLib.checkIsRoot() ?
                "root" : "no-root");

        TextView debug = findViewById(R.id.debug);
        debug.setText(SecurityCheckUtil.getSingleInstance().checkIsDebugVersion(this) ?
                "debug-version" : "release-version");

        TextView xp = findViewById(R.id.xp);
        xp.setText(EasyProtectorLib.checkIsXposedExist() ?
                (EasyProtectorLib.checkXposedExistAndDisableIt() ?
                        "has xp but closed" : "has xp but not close")
                : "no-xp");

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

        emulator = findViewById(R.id.emulator);
        emulator.setText(EmulatorCheckUtil.getSingleInstance().readSysProperty(this, null) ? "isEmulator" : "not-emulator");
        clickEmulatorDetect = findViewById(R.id.clickEmulatorDetect);
        clickEmulatorDetect.setOnClickListener(v -> detecting());

        input = findViewById(R.id.input);
        output = findViewById(R.id.output);
        output.setOnClickListener(v -> findPropertyName(input.getText().toString()));
    }

    private void detecting() {
        EmulatorCheckUtil.getSingleInstance().readSysProperty(this, new EmulatorCheckCallback() {
            @Override
            public void findEmulator(String emulatorInfo) {
                emulator.setText(emulatorInfo);
            }
        });
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


    private void findPropertyName(String propName) {
        if (TextUtils.isEmpty(propName)) {
            output.setText("property name cannot be null");
            return;
        } else {
            output.setText(CommandUtil.getSingleInstance().getProperty(propName));
        }
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
