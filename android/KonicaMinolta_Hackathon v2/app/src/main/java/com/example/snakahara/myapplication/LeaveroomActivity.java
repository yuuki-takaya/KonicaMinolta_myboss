package com.example.snakahara.myapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by TSUKASA on 16/06/20.
 */
public class LeaveroomActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeaveroomFragment fragment = new LeaveroomFragment();
        fragment.show(getSupportFragmentManager(), "alert_dialog");
    }
}
