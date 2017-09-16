package com.example.snakahara.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoadingActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private static int Bluetooth_Mode = 0;
    private static int GPS_Mode = 0;
    private final int REQUEST_PERMISSION = 1000;
    // OnCreate でロケーションマネージャを取得

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = activityManager.getRunningServices(Integer.MAX_VALUE);


        (new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> requestDic = new HashMap<String, Object>();
                int requestCode = 0;
                requestDic.put("requestCode", requestCode);
                CommManager commManager = CommManager.sharedManager(getApplicationContext());
                Map<String, Object> response = commManager.sendRequest(requestDic);
                if (response.isEmpty()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(LoadingActivity.this)
                                    .setTitle("通信エラー")
                                    .setMessage("モバイル通信をONにして\nアプリを再起動してください。")
                                    .show();
                        }
                    });
                } else {
                    //Android6.0以上かの確認
                    if(Build.VERSION.SDK_INT >= 23){
                        checkPermission();
                    }
                    else GPS_Mode = 1;

                    for(;;){
                        if (GPS_Mode == 1) break;
                    }

                    Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
                    int responseCode = (int) responseHeader.get("responseCode");
                    if (responseCode == 0) {
                        Log.w(this.getClass().toString(), "登録済み");

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                Intent intent = new Intent();
                                intent.setClassName("com.example.snakahara.myapplication", "com.example.snakahara.myapplication.MainActivity");
                                startActivity(intent);
                            }
                        });


                    } else if (responseCode == 1) {
                        Log.w(this.getClass().toString(), "未登録");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClassName("com.example.snakahara.myapplication", "com.example.snakahara.myapplication.InitialLaunchActivity");
                                startActivity(intent);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(LoadingActivity.this)
                                        .setTitle("通信エラー")
                                        .setMessage("アプリを再起動してください。")
                                        .show();

                            }
                        });
                    }
                }
            }
        })).start();

    }

    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            GPS_Mode = 1;
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
        return;
    }

    // 位置情報サービスの使用許可を求める
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, REQUEST_PERMISSION);
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GPS_Mode = 1;
                return;

            } else {
                // それでも拒否された時の対応
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoadingActivity.this)
                                .setTitle("エラー")
                                .setMessage("許可されないと\nこのアプリは起動できません。\nアプリを再起動してください。\n設定はアプリケーション一覧から\n変更できます。")
                                .show();

                    }
                });
            }
        }
    }

    @Override
    protected  void onStop(){
        super.onStop();
        System.out.println("LoadingActivity終了");
        finish();
    }
}
