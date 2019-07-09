package com.elksa.images;

import android.os.Bundle;
import android.widget.Toast;

import com.elksa.ddsample.BaseSplitActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseSplitActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R2.id.btn_test)
    void showTestMessage() {
        Toast.makeText(this, "Test Butter Knife", Toast.LENGTH_SHORT).show();
    }
}
