package com.lahm.easyprotector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lahm.library.EasyProtectorLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.hi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hello();
            }
        });
    }

    private void hello() {
        EasyProtectorLib.checkIsBeingTracedByC();
        EasyProtectorLib.checkHasLoadSO("antitrace");
        EasyProtectorLib.checkSignature(this);
        EasyProtectorLib.checkIsDebug();
        EasyProtectorLib.checkIsRoot();
        EasyProtectorLib.checkIsUsingMultiVirtualApp();
        EasyProtectorLib.checkIsXposedExist();
    }
}
