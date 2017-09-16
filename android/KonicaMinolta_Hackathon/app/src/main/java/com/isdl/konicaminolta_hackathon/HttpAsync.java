package com.isdl.konicaminolta_hackathon;

/**
 * Created by takayayuuki on 2017/09/16.
 */


import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by takayayuuki on 2016/10/20.
 */

//第一引数にがmainでexecuteを実行した時の引数
public class HttpAsync extends AsyncTask <String, Void, String>{
    private String readSt ; //getメソッドで返ってきた値（mainで〇〇.execute().get()によって取得できる）


    @Override
    protected String doInBackground(String ... param) {
        HttpURLConnection con = null;
        URL url = null;
        String urlSt = "http://192.168.128.187:3000";
        String endpoint = param[0];
        String requestType = param[1];

        try {
            // URLの作成
            url = new URL(urlSt + endpoint);

            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpURLConnection)url.openConnection();

            // リクエストメソッドの設定
            con.setRequestMethod(requestType);

            // リダイレクトを自動で許可しない設定
            con.setInstanceFollowRedirects(false);

            // URL接続からデータを読み取る場合はtrue
            con.setDoInput(true);


            // URL接続にデータを書き込む場合はtrue
//            con.setDoOutput(true);

            // 接続
            con.connect(); // ①

            InputStream in = con.getInputStream();
            readSt = readInputStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return param[0];
        return readSt;
    }



    public String readInputStream(InputStream in) throws IOException, UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        String st = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        while((st = br.readLine()) != null)
        {
            sb.append(st);
        }
        try
        {
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }
}