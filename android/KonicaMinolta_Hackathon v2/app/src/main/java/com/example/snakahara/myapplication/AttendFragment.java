package com.example.snakahara.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.w3c.dom.Text;


public class AttendFragment extends Fragment implements AttendManager.AttendManagerCallbacks {
    private final Handler handler = new Handler();
    private TextView studentInfoTextView;
    private TextView lectureInfoTextView;
    private TextView profInfoTextView;
    private TextView attendanceTextView;
    private TextView roomNameTextView;
    private ImageView attendStatusImageView;
    private AttendManager attendManager;
    private ProgressDialog progressDialog = null;
    private Button attendButton, reloadButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attend, container, false);

        attendManager = AttendManager.sharedManager(getActivity());
        attendManager.setCallbacks(this);

        studentInfoTextView = (TextView) rootView.findViewById(R.id.studentInfoTextView);
        lectureInfoTextView = (TextView) rootView.findViewById(R.id.lectureInfoTextView);
        profInfoTextView = (TextView) rootView.findViewById(R.id.profInfoTextView);
        roomNameTextView = (TextView) rootView.findViewById(R.id.roomNameTextView);
        attendanceTextView = (TextView) rootView.findViewById(R.id.attendanceTextView);
        attendStatusImageView = (ImageView) rootView.findViewById(R.id.attendanceStatusImageView);
        attendButton = (Button) rootView.findViewById(R.id.attendButton);
        reloadButton = (Button) rootView.findViewById(R.id.reloadButton);

        if(MainActivity.attendMode == 0){
            attendanceTextView.setText("---");
            roomNameTextView.setText("不明");
            lectureInfoTextView.setText("---");
            profInfoTextView.setText("担当教員 : ");
            studentInfoTextView.setText("---");
            MainActivity.attendMode = 3;
            reloadButtonEnabledChanger(true);
            attendButtonEnabledChanger(false);
            attendStatusImageView.setImageResource(R.drawable.q);
        }
        else if(MainActivity.attendMode == 1){
            roomNameTextView.setText(attendManager.roomName);
            lectureInfoTextView.setText(attendManager.lectureInfo);
            profInfoTextView.setText(attendManager.profInfo);
            attendButton.setText("退室送信");
            attendAction(0);
            reloadButtonEnabledChanger(false);
            attendButtonEnabledChanger(true);
        }

        else if(MainActivity.attendMode == -1){
            roomNameTextView.setText(attendManager.roomName);
            lectureInfoTextView.setText(attendManager.lectureInfo);
            profInfoTextView.setText(attendManager.profInfo);
            attendAction(1);
            reloadButtonEnabledChanger(false);
            attendButtonEnabledChanger(true);
        }

        else if(MainActivity.attendMode == 2){
            roomNameTextView.setText(attendManager.roomName);
            lectureInfoTextView.setText(attendManager.lectureInfo);
            profInfoTextView.setText(attendManager.profInfo);
            attendStatusImageView.setImageResource(R.drawable.q);
            reloadButtonEnabledChanger(false);
            attendButtonEnabledChanger(true);
        }

        else{
            attendanceTextView.setText("---");
            roomNameTextView.setText("不明");
            lectureInfoTextView.setText("---");
            profInfoTextView.setText("担当教員 : ");
            studentInfoTextView.setText("---");
            attendStatusImageView.setImageResource(R.drawable.q);
            reloadButtonEnabledChanger(true);
            attendButtonEnabledChanger(false);
        }

        System.out.println("CreateView");
        System.out.println(MainActivity.attendMode);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int aaa = attendManager.initialize();
                Log.w("gegew", String.valueOf(aaa));
            }
        }).start();
        System.out.println("Resume2");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Pause2");



    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Stop2");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Destroy2");
    }

    @Override
    public void showAlert(final String title, final String message ,final String button) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(button,null)
                        .show();

            }
        });
    }

    public void showListAlert(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder listDlg = new AlertDialog.Builder(getActivity());
                listDlg.setTitle("教室選択");
                listDlg.setItems(
                        attendManager.roomNameList,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // リスト選択時の処理
                                // which は、選択されたアイテムのインデックス
                                attendManager.whichroom = which;
                            }
                        });
                // 表示
                listDlg.create().show();
            }
        });
    }

    @Override
    public void changeLabel(final String name, final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (name.equals("lectureInfoTextView")) {
                    lectureInfoTextView.setText(text);
                }else if(name.equals("profInfoTextView")){
                    profInfoTextView.setText(text);
                } else if (name.equals("roomNameTextView")) {
                    roomNameTextView.setText(text);
                } else if (name.equals("studentInfoTextView")) {
                    studentInfoTextView.setText(text);
                } else if (name.equals("attendButton")) {
                    attendButton.setText(text);
                }

            }
        });

    }

    @Override
    public void attendAction(final int Mode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Mode == 0) {
                    attendanceTextView.setText("出席");
                    attendStatusImageView.setImageResource(R.drawable.ok);
                } else if (Mode == 1) {
                    attendanceTextView.setText("退室");
                    attendStatusImageView.setImageResource(R.drawable.ng);
                }
                else {
                    attendanceTextView.setText("---");
                    roomNameTextView.setText("不明");
                    lectureInfoTextView.setText("---");
                    profInfoTextView.setText("担当教員 : ");
                    attendStatusImageView.setImageResource(R.drawable.q);
                }
            }
        });
    }

    @Override
    public void reloadButtonEnabledChanger(final Boolean enableBool) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                reloadButton.setEnabled(enableBool);


            }
        });
    }

    @Override
    public void attendButtonEnabledChanger(final Boolean enableBool) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                attendButton.setEnabled(enableBool);
            }
        });
    }

    @Override
    public void showHUD(final String title, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog = ProgressDialog.show(getActivity(), title, message);

            }
        });
    }
}
