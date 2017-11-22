package com.fxl.hencodersimulate.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.fxl.hencodersimulate.R;
import com.fxl.hencodersimulate.view.jike.JiKeView;

/**
 * Created by fxl on 2017/11/18 0018.
 */

public class JikeActivity extends Activity {
    JiKeView mJiKeView;
    EditText mEditText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jike);
        mEditText = (EditText) findViewById(R.id.edittext);
        mJiKeView = (JiKeView) findViewById(R.id.jikeview);

        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = mEditText.getText().toString().trim();
                if ("".equals(number)){
                    return;
                }
                mJiKeView.setLikeNum(Integer.parseInt(number));
            }
        });
    }
}
