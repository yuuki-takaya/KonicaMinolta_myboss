package com.example.snakahara.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.os.Handler;
import android.util.Log;
import android.view.Display;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by snakahara on 16/04/24.
 */
public class AttendManager {
    private static AttendManager instance = null;
    private Context context;
    private final Handler handler;
    private AttendManagerCallbacks _attendManagerCallbacks;
    int whichroom = -1; //教室が複数検出された時に教室を判別する
    public CharSequence[] roomNameList; //教室名を格納
    String roomName = "";
    String lectureInfo = "";
    String profInfo = "";
    Map<String, Object> allBeaconInfo = new HashMap<String, Object>();  //プラットフォームからダウンロードした全ビーコン情報を格納
    Map<String, Object> BeaconInfo = new HashMap<String, Object>();     //プラットフォームからダウンロードした個ビーコン情報を格納
    Map<String, Object> majortobid = new HashMap<String, Object>();     //プラットフォームからダウンロードした全ビーコン情報のmajorとbidを対応させて格納
    ArrayList List; //プラットフォームからダウンロードした情報を取り出すために一時的にリスト化するときに使用
    Map<String, Object> map = new HashMap<String, Object>();    //プラットフォームからダウンロードした情報を取り出すために一時的にマップ化するときに使用
    Map<String, Object> room = new HashMap<String, Object>();   //プラットフォームからダウンロードした個ビーコン情報のうち、param1を格納
    int requestCode;    //サーバにどのようなリクエストを投げるかを決める変数
    Map<String, Object> requestDic = new HashMap<String, Object>();

    private AttendManager(Context context) {
        this.context = context;
        this.handler = new Handler(context.getMainLooper());
    }

    public interface AttendManagerCallbacks {
        void showAlert(String title, String message, String button);
        void showListAlert();
        void changeLabel(String name, String text);
        void attendAction(int Mode);
        void reloadButtonEnabledChanger(Boolean enableBool);
        void attendButtonEnabledChanger(Boolean enableBool);
        void showHUD(String title, String message);
    }

    public void setCallbacks(AttendManagerCallbacks attendManagerCallbacks) {
        _attendManagerCallbacks = attendManagerCallbacks;
    }

    public static AttendManager sharedManager(Context context) {
        if (instance == null) {
            instance = new AttendManager(context);
        }
        return instance;
    }

    public int initialize() {
        return getStudentInfo();
    }

    public void getBeaconInfo(){
        requestDic.clear();
        PlatformManager platformManager = PlatformManager.sharedManager(this.context);
        allBeaconInfo = platformManager.sendRequestAllBeacon(requestDic);
        if (allBeaconInfo.isEmpty()){
            _attendManagerCallbacks.showAlert("通信エラー","プラットフォームに接続できません","OK");
        }
        else if (allBeaconInfo.get("result").toString().equals("00")) {
            List = (ArrayList) allBeaconInfo.get("tags");
            for (int i = 0; i < List.size(); i++) {
                map = (HashMap) List.get(i);
                majortobid.put(map.get("bid2").toString(), map.get("bid"));
            }
        }
        else {
            _attendManagerCallbacks.showAlert("エラー","プラットフォームとの通信に\n問題があります","OK");
        }
    }

    public int getStudentInfo() {
        requestDic.clear();
        requestCode = 5;
        requestDic.put("requestCode", requestCode);
        CommManager commManager = CommManager.sharedManager(this.context);
        Map<String, Object> response = commManager.sendRequest(requestDic);
        if (response.isEmpty()) {
            _attendManagerCallbacks.showAlert("通信エラー", "サーバに接続できません。\nネットワークを確認して下さい。", "OK!");
            return -1;
        } else {
            Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
            Map<String, Object> responseResponse = (Map<String, Object>) response.get("response");
            int responseCode = (int) responseHeader.get("responseCode");
            switch (responseCode) {
                case 0:
                    Log.w("AttendManager", "生徒情報取得成功");
                    String textViewString = (String) responseResponse.get("studentID") + "：" + (String) responseResponse.get("studentName");
                    _attendManagerCallbacks.changeLabel("studentInfoTextView", textViewString);
                    return 0;
                case 1:
                    Log.w("AttendManager", "生徒情報取得失敗");
                    _attendManagerCallbacks.showAlert("生徒情報取得失敗", "エラーコード1", "OK!");
                    return 1;
                default:
                    return 2;
            }
        }

    }

    public void room() {
        whichroom = -1; //教室が複数検知された時に教室を判断するための変数 毎回初期化しないと複数回教室リストが呼ばれた時にバグ
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}

        if (MainActivity.beaconcount == 0) {  //ビーコンを受信していないとき
            _attendManagerCallbacks.showAlert("エラー", "ビーコンを受信できません。\n・位置情報サービスが有効\n・Bluetoothがオン\nになっているかを確認して下さい。\n" +
                    "また本アプリは教室外では" + "\n利用できません", "OK!");
            _attendManagerCallbacks.reloadButtonEnabledChanger(true);
            _attendManagerCallbacks.attendAction(2);
            return;
        } else if (MainActivity.beaconcount == 1) { //1つの教室を検知していた場合
            requestDic.clear();
            PlatformManager platformManager = PlatformManager.sharedManager(this.context);
            BeaconInfo = platformManager.sendRequestIndividualBeacon(requestDic, (Integer.valueOf(String.valueOf(majortobid.get(String.valueOf(MainActivity.clBeacon[0].major))))));
            if (BeaconInfo.isEmpty())  {
                _attendManagerCallbacks.showAlert("通信エラー", "プラットフォームに接続できません", "OK!");
                _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                _attendManagerCallbacks.attendAction(2);
                return;
            }
            else if (BeaconInfo.get("result").toString().equals("00")) {
                List = (ArrayList) BeaconInfo.get("tags");
                System.out.println(List);
                room = (HashMap) List.get(0);
                roomName = String.valueOf(room.get("param1"));
            }
            else {
                _attendManagerCallbacks.showAlert("通信エラー", "プラットフォームとの通信に\n問題が発生しています", "OK");
                _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                _attendManagerCallbacks.attendAction(2);
                return;
            }
        } else {   //複数の教室を検知していた場合
            //MainActivityで受信したビーコン情報を別の配列に格納し直す　MainActivityのclBeaconは格納順番が変わる可能性があるため
            CLBeacon[] getclbeacon = new CLBeacon[MainActivity.beaconcount];
            for (int i = 0; i < MainActivity.beaconcount; i++) {
                getclbeacon[i] = MainActivity.clBeacon[i];
            }
            roomNameList = new CharSequence[getclbeacon.length];
            for (int i = 0; i < getclbeacon.length; i++) {
                requestDic.clear();
                PlatformManager platformManager = PlatformManager.sharedManager(this.context);
                BeaconInfo = platformManager.sendRequestIndividualBeacon(requestDic, (Integer.valueOf(String.valueOf(majortobid.get(String.valueOf(getclbeacon[i].major))))));
                if (BeaconInfo.isEmpty()) {
                    _attendManagerCallbacks.showAlert("通信エラー", "プラットフォームに接続できません。\nネットワークを確認して下さい。", "OK");
                    _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                    _attendManagerCallbacks.attendAction(2);
                    return;
                } else if (BeaconInfo.get("result").toString().equals("00")) {
                    List = (ArrayList) BeaconInfo.get("tags");
                    room = (HashMap) List.get(0);
                    roomNameList[i] = (String) room.get("param1");
                }else {
                    _attendManagerCallbacks.showAlert("通信エラー", "プラットフォームとの通信に\n問題が発生しています", "OK");
                    _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                    _attendManagerCallbacks.attendAction(2);
                    return;
                }
            }
            _attendManagerCallbacks.showListAlert();
            for (;;) {
                if (whichroom != -1) break;
            }
            roomName = String.valueOf(roomNameList[whichroom]);
            roomNameList = null;    //必要ないため初期化
        }

        //教室名を取得すればビーコンの情報は必要ないから
        MainActivity.beaconcount = 0;
        MainActivity.clBeacon = new CLBeacon[10];

        requestDic.clear();
        requestCode = 4;
        requestDic.put("requestCode", requestCode);
        requestDic.put("room", roomName);
        CommManager commManager = CommManager.sharedManager(this.context);
        Map<String, Object> response = commManager.sendRequest(requestDic);
        if (response.isEmpty()) {
            _attendManagerCallbacks.showAlert("通信エラー", "サーバに接続できません。\nネットワークを確認して下さい。", "OK!");
            _attendManagerCallbacks.reloadButtonEnabledChanger(true);
            _attendManagerCallbacks.attendAction(2);
            return;
        } else {
            Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
            Map<String, Object> responseResponse = (Map<String, Object>) response.get("response");
            int responseCode = (int) responseHeader.get("responseCode");

            if (responseCode == 5) {
                _attendManagerCallbacks.showAlert("エラー", "現在は講義が行われていません", "OK");
                _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                _attendManagerCallbacks.attendAction(2);
                return;
            } else if ((Integer) responseResponse.get("attendMode") == 1) {
                lectureInfo = (Integer) responseResponse.get("timeID") + "講時：" + (String) responseResponse.get("subject");
                profInfo = ("担当教員 : "+(String)responseResponse.get("profName"));
                _attendManagerCallbacks.changeLabel("profInfoTextView",profInfo);
                _attendManagerCallbacks.changeLabel("lectureInfoTextView", lectureInfo);
                _attendManagerCallbacks.changeLabel("roomNameTextView", roomName);
                _attendManagerCallbacks.attendAction(0);
                _attendManagerCallbacks.changeLabel("attendButton", "退室送信");
                _attendManagerCallbacks.attendButtonEnabledChanger(true);
                _attendManagerCallbacks.reloadButtonEnabledChanger(false);
                MainActivity.attendMode = 1;
                return;
            } else if ((Integer) responseResponse.get("attendMode") == 0) {
                lectureInfo = (Integer) responseResponse.get("timeID") + "講時：" + (String) responseResponse.get("subject");
                profInfo = ("担当教員 : "+(String)responseResponse.get("profName"));
                _attendManagerCallbacks.changeLabel("profInfoTextView",profInfo);
                _attendManagerCallbacks.changeLabel("lectureInfoTextView", lectureInfo);
                _attendManagerCallbacks.changeLabel("roomNameTextView", roomName);
                _attendManagerCallbacks.attendAction(1);
                _attendManagerCallbacks.attendButtonEnabledChanger(true);
                _attendManagerCallbacks.reloadButtonEnabledChanger(false);
                MainActivity.attendMode = -1;
                return;
            } else {
                switch (responseCode) {
                    case 0:
                        Log.w("AttendManager", "講義データ取得成功");
                        lectureInfo = (Integer) responseResponse.get("timeID") + "講時：" + (String) responseResponse.get("subject");
                        profInfo = ("担当教員 : "+(String)responseResponse.get("profName"));
                        _attendManagerCallbacks.changeLabel("profInfoTextView",profInfo);
                        _attendManagerCallbacks.changeLabel("lectureInfoTextView", lectureInfo);
                        _attendManagerCallbacks.changeLabel("roomNameTextView", roomName);
                        _attendManagerCallbacks.reloadButtonEnabledChanger(false);
                        _attendManagerCallbacks.attendButtonEnabledChanger(true);
                        MainActivity.attendMode = 2;
                        break;
                    case 1:
                        Log.w("AttendManager", "講義データ取得失敗");
                        _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                        _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                        break;
                    case 5:
                        Log.w("AttendManager", "講義が登録されていない");
                        _attendManagerCallbacks.showAlert("エラー", "現在は講義が行われていません", "OK");
                        _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                        break;
                    default:
                        _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                        _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                        break;
                }
            }
        }
    }


    public void attend() {
        requestDic.clear();
        requestCode = 1;
        requestDic.put("requestCode", requestCode);
        requestDic.put("room", roomName);
        CommManager commManager = CommManager.sharedManager(this.context);
        Map<String, Object> response = commManager.sendRequest(requestDic);

        if (response.isEmpty()) {
            _attendManagerCallbacks.showAlert("通信エラー", "サーバに接続できません。\nネットワークを確認して下さい。", "OK!");
            _attendManagerCallbacks.attendButtonEnabledChanger(true);
            return;
        } else {
            Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
            int responseCode = (int) responseHeader.get("responseCode");
            switch (responseCode) {
                case 0:
                    Log.w("AttendManager", "出席データ登録成功");
                    _attendManagerCallbacks.showAlert("", "出席しました", "OK");
                    MainActivity.attendMode = 1;
                    _attendManagerCallbacks.attendAction(0);
                    _attendManagerCallbacks.changeLabel("attendButton", "退室送信");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                case 1:
                    Log.w("AttendManager", "DBエラー（例外発生）");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                case 2:
                    Log.w("AttendManager", "DBから学生IDを取得失敗");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                case 3:
                    Log.w("AttendManager", "DBから教室名を取得失敗");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                default:
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
            }
        }
    }

    public void leave_room() {
        requestDic.clear();
        requestCode = 2;
        requestDic.put("requestCode", requestCode);
        requestDic.put("room", roomName);
        CommManager commManager = CommManager.sharedManager(this.context);
        Map<String, Object> response = commManager.sendRequest(requestDic);

        if (response.isEmpty()) {
            _attendManagerCallbacks.showAlert("通信エラー", "サーバに接続できません。\nネットワークを確認して下さい。", "OK!");
            _attendManagerCallbacks.attendButtonEnabledChanger(true);
            return;
        } else {
            Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
            int responseCode = (int) responseHeader.get("responseCode");
            switch (responseCode) {
                case 0:
                    Log.w("AttendManager", "退室データ登録成功");
                    _attendManagerCallbacks.showAlert("", "退室しました", "OK");
                    MainActivity.attendMode = 3;
                    _attendManagerCallbacks.attendAction(1);
                    _attendManagerCallbacks.changeLabel("attendButton","出席送信");
                    _attendManagerCallbacks.reloadButtonEnabledChanger(true);
                    roomName = "";  //退室したら教室名を初期化する
                    break;
                case 1:
                    Log.w("AttendManager", "DBエラー（例外発生）");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                case 2:
                    Log.w("AttendManager", "DBから学生IDを取得失敗");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                case 3:
                    Log.w("AttendManager", "DBから教室名を取得失敗");
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
                default:
                    _attendManagerCallbacks.showAlert("エラー", "もう一度やり直してください", "OK");
                    _attendManagerCallbacks.attendButtonEnabledChanger(true);
                    break;
            }
        }
    }

    //ボタンの使用可能状態を変える関数
    public void changeButton(int i,Boolean judge){
        if(i == 0){
            if(judge == true) _attendManagerCallbacks.attendButtonEnabledChanger(true);
            else              _attendManagerCallbacks.attendButtonEnabledChanger(false);
        }
        else{
            if(judge == true) _attendManagerCallbacks.reloadButtonEnabledChanger(true);
            else              _attendManagerCallbacks.reloadButtonEnabledChanger(false);
        }
    }

    public Map getlecturehistory() {
       requestDic.clear();
        requestCode = 6;
        requestDic.put("requestCode", requestCode);
        CommManager commManager = CommManager.sharedManager(this.context);
        Map<String, Object> response = commManager.sendRequest(requestDic);
        if (response.isEmpty()) {
            _attendManagerCallbacks.showAlert("通信エラー", "サーバに接続できません。\nネットワークを確認して下さい。", "OK!");
            return null;
        } else {
            Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
            Map<String, Object> responseResponse = (Map<String, Object>) response.get("response");
            int responseCode = (int) responseHeader.get("responseCode");
            switch (responseCode) {
                case 0:
                    Log.w("AttendManager", "履歴情報取得成功");
                    return response;
                case 1:
                    Log.w("AttendManager", "履歴情報取得失敗");
                    return null;
                default:
                    return null;
            }
        }
    }
}
