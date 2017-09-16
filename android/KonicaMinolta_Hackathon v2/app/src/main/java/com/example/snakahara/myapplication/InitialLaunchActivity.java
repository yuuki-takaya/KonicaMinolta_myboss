package com.example.snakahara.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class InitialLaunchActivity extends AppCompatActivity {
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_launch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Push通知用のtopicの登録
        FirebaseMessaging.getInstance().subscribeToTopic("Attendance_Android");

        final String MATCH_ID = "^[0-9a-zA-Z]{8}";
        final EditText studentIDEditText = (EditText) findViewById(R.id.studentIDEditText);
        final EditText studentFirstNameEditText = (EditText) findViewById(R.id.studentFirstNameEditText);
        final EditText studentLastNameEditText = (EditText) findViewById(R.id.studentLastNameEditText);
        final Button setButton = (Button) findViewById(R.id.setButton);
        setButton.setEnabled(false);

        assert studentIDEditText != null;
        assert studentFirstNameEditText != null;
        assert studentLastNameEditText != null;
        studentIDEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Check if 's' is empty
                if (!studentIDEditText.getText().toString().isEmpty() && !studentFirstNameEditText.getText().toString().isEmpty() && !studentLastNameEditText.getText().toString().isEmpty()){
                    setButton.setEnabled(true);
                }
                else{
                    setButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        studentFirstNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Check if 's' is empty
                if (!studentIDEditText.getText().toString().isEmpty() && !studentFirstNameEditText.getText().toString().isEmpty() && !studentLastNameEditText.getText().toString().isEmpty()){
                    setButton.setEnabled(true);
                }
                else{
                    setButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String studentID = studentIDEditText.getText().toString().toUpperCase();
                if (studentID.matches(MATCH_ID)){

                    final String studentName = (studentLastNameEditText.getText().toString()+" "+studentFirstNameEditText.getText().toString());
                    String message = "学生ID："+studentID+"\n名前："+studentName+"\nでよろしいですか？";
                    // クリック時の処理
                    new AlertDialog.Builder(InitialLaunchActivity.this)
                            .setTitle("入力情報確認")
                            .setMessage(message)
                            .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    final Map<String, Object> requestDic = new HashMap<String, Object>();
                                    final int requestCode = 3;
                                    requestDic.put("requestCode", requestCode);
                                    requestDic.put("name",studentName);
                                    requestDic.put("studentID",studentID);

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            CommManager commManager = CommManager.sharedManager(getApplicationContext());
                                            Map<String,Object> response = commManager.sendRequest(requestDic);
                                            if (!response.isEmpty()) {
                                                Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
                                                Map<String, Object> responseResponse = (Map<String, Object>) response.get("response");
                                                int responseCode = (int) responseHeader.get("responseCode");
                                                if (responseCode == 0) {
                                                    Intent intent = new Intent();
                                                    intent.setClassName("com.example.snakahara.myapplication", "com.example.snakahara.myapplication.MainActivity");
                                                    startActivity(intent);
                                                }else {
                                                    Log.w("InitialLaunchActivity", "登録エラー");
                                                    final String name = (String)responseResponse.get("studentName");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new AlertDialog.Builder(InitialLaunchActivity.this)
                                                                    .setTitle("エラー")
                                                                    .setMessage("この学生IDはすでに登録されています\n学生ID:"+studentID+" 名前:"+name)
                                                                    .show();

                                                        }
                                                    });
                                                }
                                            }else{
                                                Log.w("InitialLaunchActivity", "登録エラー");
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(InitialLaunchActivity.this)
                                                                .setTitle("エラー")
                                                                .setMessage("登録エラーが発生しました。")
                                                                .show();

                                                    }
                                                });
                                            }

                                        }
                                    }).start();


                                }
                            })
                            .setNegativeButton("いいえ", null)
                            .show();

                }
                else{
                            new AlertDialog.Builder(InitialLaunchActivity.this)
                                    .setTitle("エラー")
                                    .setMessage("学生IDの形式が不正です。")
                                    .setPositiveButton("OK",null)
                            .show();

                }

            }
        });

    }
}
