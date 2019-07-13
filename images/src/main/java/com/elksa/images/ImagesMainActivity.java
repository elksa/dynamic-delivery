package com.elksa.images;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elksa.ddsample.BaseSplitActivity;

public class ImagesMainActivity extends BaseSplitActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTestMessage();
            }
        });
    }

    void showTestMessage() {
        Toast.makeText(this, "Test Butter Knife", Toast.LENGTH_SHORT).show();
    }
}
