package com.example.snakahara.myapplication;

import android.app.Application;

public class Common extends Application {
    int AttendMode;
    int Attendmajor;
    int Attendminor;
    public void init(){
        AttendMode = 0;
        Attendmajor = 0;
        Attendminor = 0;
    }
}
