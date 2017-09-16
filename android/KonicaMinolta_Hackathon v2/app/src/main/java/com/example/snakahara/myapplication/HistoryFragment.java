package com.example.snakahara.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class HistoryFragment extends Fragment {

    private Context context;
    private List<Map<String, String>> list;
    private AttendManager attendManager;
    int flag = -1;//-1:初期値 0:履歴読み込み成功 1:履歴読み込み失敗
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("HistoryFragment");
        View rootview = inflater.inflate(R.layout.fragment_history, container, false);
        context = getContext();
        ListView histlist = (ListView) rootview.findViewById(R.id.historylist);
        getlecturehistory();
        for (;;){
            if (flag != -1) break;
        }
        flag = -1;
        SimpleAdapter adapter = new SimpleAdapter(context,list,android.R.layout.simple_list_item_2, new String[]{"lectureName", "Date"},new int[] {android.R.id.text1, android.R.id.text2});
        histlist.setAdapter(adapter);

        // アイテムクリック時のイベントを追加
        histlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {

                // 選択アイテムを取得
                ListView listView = (ListView)parent;
                Map<String,String> item = new HashMap<String, String>();
                item = (HashMap)listView.getItemAtPosition(pos);

                showAlert("詳細情報",item.get("lectureName"),item.get("Date"),item.get("startTime"),item.get("endTime"),"OK");

            }
        });

        return rootview;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void getlecturehistory(){
        attendManager = AttendManager.sharedManager(getActivity());
        list = new ArrayList<Map<String, String>>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> response = attendManager.getlecturehistory();
                if(response != null) {
                    Map<String, Object> responseHeader = (Map<String, Object>) response.get("header");
                    Map<String, Object> responseResponse = (Map<String, Object>) response.get("response");
                    Map<String, Object> count = (Map<String, Object>) responseResponse.get("0");
                    int historycount = (int) count.get("count");
                    for(int i = 0;i < historycount;i++){
                        Map<String, Object> lectureHistory = (Map<String, Object>) responseResponse.get(String.valueOf(i));
                        Map<String, String> map = new HashMap<String, String>();
                        String lectureName = lectureHistory.get("subject").toString();
                        String lectureDate = lectureHistory.get("Date").toString();
                        String startTime = lectureHistory.get("startTime").toString();
                        String endTime = lectureHistory.get("endTime").toString();
                        if (endTime.equals("0")) endTime = "不明";     //退室時間が登録されていない場合endTimeに0が入る
                        map.put("lectureName", lectureName);
                        map.put("Date", lectureDate);
                        map.put("startTime", startTime);
                        map.put("endTime", endTime);
                        list.add(map);
                    }
                    flag = 0;
                }
                else flag = 1;
            }
        }).start();
    }

    public void showAlert(final String title, final String lectureName, final String Date, final String startTime, final String endTime ,final String button) {
        handler.post(new Runnable() {
            String message = "講義名 : "+lectureName+"\n日付 : "+Date+"\n出席時刻 : "+startTime+"\n退室時刻 : "+endTime;
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
}
