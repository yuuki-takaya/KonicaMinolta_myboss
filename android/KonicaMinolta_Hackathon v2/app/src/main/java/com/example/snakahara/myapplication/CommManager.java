package com.example.snakahara.myapplication;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by snakahara on 16/04/24.
 */
public class CommManager {
    private static CommManager instance = null;
    private String deviceID;    //Android端末のUUID
    private Map<String,Object> recvResponses = new HashMap<String,Object>();
    private CommManager(Context context){
        Installation installation = new Installation();
        this.deviceID = installation.id(context);
    }

    public static CommManager sharedManager(Context context){
        if (instance == null){
            instance = new CommManager(context);
        }
        return instance;
    }


    public Map<String,Object> sendRequest(Map<String,Object> requestDic){
        Map<String,Object> headerDic = new HashMap<String,Object>();
        headerDic.put("uuid",this.deviceID);

        Map<String,Object> sendRequestDic = new HashMap<String,Object>();
        sendRequestDic.put("header",headerDic);
        sendRequestDic.put("request",requestDic);

        String requestString = makeJSONString(sendRequestDic);

        Log.w("aaa",requestString);

        HttpURLConnection con = null;
        URL url = null;
        String urlSt = "http://v157-7-129-202.myvps.jp:40000";
        //String urlSt = "http://172.20.11.167:40000";
        try {
            // URLの作成
            url = new URL(urlSt);
            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            // URL接続からデータを読み取る場合はtrue
            con.setDoInput(true);
            // URL接続にデータを書き込む場合はtrue
            con.setDoOutput(true);
            // タイムアウト設定
            con.setConnectTimeout(5000);
            con.setUseCaches( false );
            con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type","application/json");
            // リクエストメソッドの設定
            requestString = requestString + "\r\n";
            con.setRequestProperty("Content-Length", String.valueOf(requestString.getBytes("UTF-8").length));
            // 接続
            con.connect();

            DataOutputStream os = new DataOutputStream(con.getOutputStream());
            os.write(requestString.getBytes("UTF-8"));
            os.flush();
            os.close();

            if( con.getResponseCode() == HttpURLConnection.HTTP_OK ){
                StringBuffer responseJSON = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    responseJSON.append(inputLine);
                }
                ObjectMapper mapper = new ObjectMapper();
                Log.w("aaa",responseJSON.toString());
                this.recvResponses = mapper.readValue(responseJSON.toString(), new TypeReference<Map<String,Object>>() {});
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.w("CommManager","通信エラー");
            this.recvResponses = new HashMap<String,Object>();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("CommManager","通信エラー");
            this.recvResponses = new HashMap<String,Object>();
        }

        return this.recvResponses;
    }

    private static String makeJSONString(Map<String,Object> valueMap){

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(valueMap);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
