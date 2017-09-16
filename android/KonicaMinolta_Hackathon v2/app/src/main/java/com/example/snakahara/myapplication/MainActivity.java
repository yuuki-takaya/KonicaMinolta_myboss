package com.example.snakahara.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private BeaconManager beaconManager;
    private final Handler handler = new Handler();
    protected static final String TAG = "MonitoringActivity";
    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String BEACON_UUID = "3DBD0100-1DDD-46AC-9E40-6B530FA0DF94";
    //private static final String BEACON_UUID = "00000000-E30A-1001-B000-001C4D99F26D";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AttendManager attendManager = null;
    private Timer mainTimer;                    //タイマー用
    private MainTimerTask mainTimerTask;        //タイマタスククラス
    static int attendMode = 0;                         //-1:退室 0:起動直後 1:出席 2:教室情報取得後 3:教室情報取得前
    static int beaconcount = 0;                        //検出したビーコンの数
    int beaconjudge = 1;                        //検出したビーコンを配列に格納するかを判断する変数
    private Common common;                      //アクティビティ間のグローバル変数
    static CLBeacon[] clBeacon = new CLBeacon[10];
    private Region region1;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        //タイマー設定
        this.mainTimer = new Timer();
        this.mainTimerTask = new MainTimerTask();
        this.mainTimer.schedule(mainTimerTask, 200);

        //共通変数の利用，初期化
        common = (Common) getApplication();
        common.init();

        System.out.println("Create");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Start");
        attendMode = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resume");
        beaconManager.bind(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
        System.out.println("Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destroy");
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("Restart");
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconcount = 0;
        clBeacon = new CLBeacon[10];

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                    System.out.println("レンジング開始");
                } catch (RemoteException e) {
                }
                Log.i(TAG, "didEnterRegion");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "didExitRegion");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });


        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i(TAG, "didRangingBeacons");
                // 検出したビーコンの情報を全部Logに書き出す
                beaconjudge = 1;
                for (Beacon beacon : beacons) {
                    if (beaconcount == 0) {     //１つ目のビーコンを検出した場合
                        clBeacon[beaconcount] = new CLBeacon();
                        Log.d("MyActivity", "UUID:" + beacon.getId1() + ", major:" + beacon.getId2() + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance());
                        clBeacon[beaconcount].major = beacon.getId2().toInt();
                        clBeacon[beaconcount].minor = beacon.getId3().toInt();
                        clBeacon[beaconcount].rssi = beacon.getRssi();
                        beaconcount += 1;
                    } else {                    //２つ目以降ビーコンを検出した場合
                        for (int i = 0; i < beaconcount; i++) {
                            if (beacon.getId2().toInt() == clBeacon[i].major) { //同じmajor値のビーコンを検出していないかの判別
                                beaconjudge = 0;
                            }
                        }
                        if (beaconjudge == 0) {}
                        else {                //同じmajor値がなかった場合配列に格納
                            clBeacon[beaconcount] = new CLBeacon();
                            Log.d("MyActivity", "UUID:" + beacon.getId1() + ", major:" + beacon.getId2() + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance());
                            clBeacon[beaconcount].major = beacon.getId2().toInt();
                            clBeacon[beaconcount].minor = beacon.getId3().toInt();
                            clBeacon[beaconcount].rssi = beacon.getRssi();
                            beaconcount += 1;
                        }
                        beaconjudge = 1;
                    }
                }
            }
        });
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AttendFragment();
                case 1:
                    return new SettingsFragment();
                case 2:
                    return new HistoryFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "出席";
                case 1:
                    return "設定";
                case 2:
                    return "履歴";
            }
            return null;
        }
    }

    public void buttonclick(View view) {
        attendManager = AttendManager.sharedManager(getApplicationContext());
        switch (view.getId()) {
            case R.id.reloadButton:
                if (Bluetooth_conf() == 0){
                    showAlert("エラー","BluetoothがOFFになっています。\nBluetoothをONにしてください。","OK");
                }
                else if(GPS_conf() == 0){
                    showAlert("エラー","GPSがOFFになっています。\nGPSをONにしてください。","OK");
                }
                else{
                    ProgressDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                beaconManager.startMonitoringBeaconsInRegion(new Region("ISDL", Identifier.parse(BEACON_UUID), null, null));
                                System.out.println("領域監視開始");
                            } catch (RemoteException e) {
                            }
                            attendManager.room();
                            stopMonitoringBeaconsInRegion();
                            progressDialog.dismiss();
                        }
                    }).start();
                }
                break;

            case R.id.attendButton:
                if (attendMode == 2 || attendMode == -1) {     //出席送信ボタンを押した時
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            attendManager.changeButton(0, false);
                            attendManager.attend();
                        }
                    }).start();
                } else {   //退席送信ボタンを押した時
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            attendManager.changeButton(0, false);
                            attendManager.leave_room();
                        }
                    }).start();
                }
                break;
        }
    }

    //タイマー機能
    public class MainTimerTask extends TimerTask {

        public void run() {
            attendManager = AttendManager.sharedManager(getApplicationContext());
            //ここに定周期で実行したい処理を記述します
            new Thread(new Runnable() {
                @Override
                public void run() {
                    attendManager.getBeaconInfo();
                }
            }).start();
        }
    }


    public void stopMonitoringBeaconsInRegion(){   //ビーコンの監視を止める
        try {
            beaconManager.stopRangingBeaconsInRegion(new Region("ISDL", Identifier.parse(BEACON_UUID), null, null));
            System.out.println("レンジング終了");
        } catch (RemoteException e) {}

        try {
            beaconManager.stopMonitoringBeaconsInRegion(new Region("ISDL", Identifier.parse(BEACON_UUID), null, null));
            System.out.println("領域監視終了");
        } catch (RemoteException e) {}

    }

    public void ProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("処理中");
        progressDialog.setMessage("ビーコンを検索しています・・・");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public void showAlert(final String title, final String message ,final String button) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button,null)
                .show();
    }

    int Bluetooth_conf(){
        BluetoothAdapter Bt = BluetoothAdapter.getDefaultAdapter();
        if(Bt.isEnabled()) return 1;
        else return 0;
    }

    int GPS_conf(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return 1;
        else return 0;
    }
}
