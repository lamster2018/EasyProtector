package com.lahm.easyprotector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lahm.library.CheckMultiUtil;
import com.lahm.library.EasyProtectorLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView one = findViewById(R.id.one);
        one.setText(EasyProtectorLib.checkIsUsingMultiVirtualApp() ?
                "uid检测有多开" : "uid检测正常");
        TextView two = findViewById(R.id.two);
        two.setText(CheckMultiUtil.getSingleInstance().checkByMultiApkPackageName() ?
                "maps检测有多开" : "maps检测正常");
        TextView three = findViewById(R.id.three);
        three.setText(CheckMultiUtil.getSingleInstance().checkByOriginApkPackageName(this) ?
                "包名检测有多开" : "包名检测正常");

        TextView root = findViewById(R.id.root);
        root.setText(EasyProtectorLib.checkIsRoot() ? "有root权限" : "无root权限或root不成功");

        TextView debug = findViewById(R.id.debug);
        debug.setText(EasyProtectorLib.checkIsDebug(this) ? "debug" : "no-debug");

        TextView xp = findViewById(R.id.xp);
        xp.setText(EasyProtectorLib.checkIsXposedExist() ?
                (EasyProtectorLib.checkXposedExistAndDisableIt() ?
                        "有xp框架但关闭成功" : "有xp框架但关闭失败")
                : "无xp框架");

        TextView traced = findViewById(R.id.traced);
        traced.setText(EasyProtectorLib.checkIsBeingTracedByJava() ? "being traced" : "safe");

        final Button loadSO = findViewById(R.id.loadSO);
        loadSO.setOnClickListener(v -> {
            EasyProtectorLib.checkIsBeingTracedByC();
            loadSO.setText("log中查看traceId");
            loadSO.setClickable(false);
        });

        Button test = findViewById(R.id.test);
        test.setOnClickListener(v -> gg());
    }

    private void gg() {

    }


}
