package com.lahm.easyprotector;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

import com.lahm.library.EasyProtectorLib;
import com.lahm.library.EmulatorCheckCallback;
import com.lahm.library.SecurityCheckUtil;
import com.lahm.library.VirtualApkCheckUtil;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.checkByPrivateFilePath).setOnClickListener(this);
        findViewById(R.id.checkByOriginApkPackageName).setOnClickListener(this);
        findViewById(R.id.checkByMultiApkPackageName).setOnClickListener(this);
        findViewById(R.id.checkByHasSameUid).setOnClickListener(this);
        findViewById(R.id.checkByPortListening).setOnClickListener(this);
        findViewById(R.id.checkByCreateLocalServerSocket).setOnClickListener(this);
        findViewById(R.id.checkRoot).setOnClickListener(this);
        findViewById(R.id.checkDebuggable).setOnClickListener(this);
        findViewById(R.id.checkDebuggerAttach).setOnClickListener(this);
        findViewById(R.id.checkTracer).setOnClickListener(this);
        findViewById(R.id.checkXP).setOnClickListener(this);
        findViewById(R.id.readSysProperty).setOnClickListener(this);
        findViewById(R.id.test).setOnClickListener(this);
    }

    private void forTest() {
        //only for test
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test:
                forTest();
                break;
            case R.id.checkByPrivateFilePath:
                TextView v1 = findViewById(R.id.v1);
                v1.setText(VirtualApkCheckUtil.getSingleInstance().checkByPrivateFilePath(this, null) ?
                        "privatePath-NO" : "privatePath-OK");
                break;
            case R.id.checkByOriginApkPackageName:
                TextView v2 = findViewById(R.id.v2);
                v2.setText(VirtualApkCheckUtil.getSingleInstance().checkByOriginApkPackageName(this, null) ?
                        "packageName-NO" : "packageName-OK");
                break;
            case R.id.checkByMultiApkPackageName:
                TextView v3 = findViewById(R.id.v3);
                v3.setText(VirtualApkCheckUtil.getSingleInstance().checkByMultiApkPackageName(null) ?
                        "maps-NO" : "maps-OK");
                break;
            case R.id.checkByHasSameUid:
                TextView v4 = findViewById(R.id.v4);
                v4.setText(VirtualApkCheckUtil.getSingleInstance().checkByHasSameUid(null) ?
                        "uid-NO" : "uid-OK");
                break;
            case R.id.checkByPortListening:
                VirtualApkCheckUtil.getSingleInstance().checkByPortListening("port", null);
                TextView v5 = findViewById(R.id.v5);
                v5.setText("port listening");
                break;
            case R.id.checkByCreateLocalServerSocket:
                TextView v6 = findViewById(R.id.v6);
                v6.setText(EasyProtectorLib.checkIsRunningInVirtualApk(getPackageName(), null) ?
                        "LocalServerSocket-NO" : "LocalServerSocket-OK");
                break;
            case R.id.checkRoot:
                TextView r1 = findViewById(R.id.r1);
                r1.setText(EasyProtectorLib.checkIsRoot() ?
                        "rooted" : "no-root");
                break;
            case R.id.checkDebuggable:
                TextView d1 = findViewById(R.id.d1);
                d1.setText(EasyProtectorLib.checkIsDebug(this) ?
                        "debuggable" : "release");
                break;
            case R.id.checkDebuggerAttach:
                TextView d2 = findViewById(R.id.d2);
                d2.setText(SecurityCheckUtil.getSingleInstance().checkIsUsbCharging(MainActivity.this) ?
                        SecurityCheckUtil.getSingleInstance().checkIsDebuggerConnected() ?
                                "debugger-connect！！" : "only-usb-charging"
                        : "only-charging");
                break;
            case R.id.checkTracer:
                EasyProtectorLib.checkIsBeingTracedByC();
                TextView d3 = findViewById(R.id.d3);
                d3.setText("see log");
                break;
            case R.id.checkXP:
                TextView x1 = findViewById(R.id.x1);
                x1.setText(EasyProtectorLib.checkIsXposedExist() ?
                        "shutdown xp success" : "failed");
                break;
            case R.id.readSysProperty:
                TextView e1 = findViewById(R.id.e1);
                EasyProtectorLib.checkIsRunningInEmulator(this, new EmulatorCheckCallback() {
                    @Override
                    public void findEmulator(String emulatorInfo) {
                        e1.setText(emulatorInfo);
                    }
                });
                break;
        }
    }
}
