package com.example.snakahara.myapplication;

import android.content.Context;
import android.util.Base64;
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
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by TSUKASA on 16/12/07.
 */
public class PlatformManager {
    private static PlatformManager instance = null;
    private String deviceID;    //Android端末のUUID
    private Map<String,Object> recvResponses = new HashMap<String,Object>();
    private static int serviceid = 3;               //クリーンビーコンプラットフォームで使用するserviceid
    private static int subid = 1;                   //クリーンビーコンプラットフォームで使用するsubid
    private static String userID = "VGt_2gg1";      //クリーンビーコンプラットフォームで使用するユーザID
    private static String password = "q3KFqm0*";    //クリーンビーコンプラットフォームで使用するパスワード
    private static String Salt = "jfS%G!Lv";                       //ハッシュ関数のソルト値

    private PlatformManager(Context context){
        Installation installation = new Installation();
        this.deviceID = installation.id(context);
    }

    public static PlatformManager sharedManager(Context context){
        if (instance == null){
            instance = new PlatformManager(context);
        }
        return instance;
    }

    public Map<String,Object> sendRequestAllBeacon(Map<String,Object> requestDic ){   //patarn 0:全ビーコン情報取得 1:ビーコン毎の情報取得

        String encodeAuthorization = Base64.encodeToString((userID + ":" + password).getBytes(), Base64.NO_WRAP);   //Basic認証に使用
        String HashString = Salt+"serviceid"+String.valueOf(serviceid)+"subid"+String.valueOf(subid);   //ハッシュ化する文字列を生成
        HashString = makeHashString(HashString);    //文字列をハッシュ値に変換
        Map<String,Object> sendRequestDic = new HashMap<String,Object>();
        sendRequestDic.put("h",HashString);
        sendRequestDic.putAll(requestDic);
        sendRequestDic.put("subid",subid);
        sendRequestDic.put("serviceid",serviceid);


        String requestString = makeJSONString(sendRequestDic);
        Log.w("aaa",requestString);
        HttpsURLConnection con = null;
        URL url = null;
        String urlSt = "";
        urlSt = "https://gw11.clean-beacon.com/bapi/1.0.0/beacon_filter.json";
        try {
            // URLの作成
            url = new URL(urlSt);
            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            // URL接続からデータを読み取る場合はtrue
            con.setDoInput(true);
            // URL接続にデータを書き込む場合はtrue
            con.setDoOutput(true);
            // タイムアウト設定
            con.setConnectTimeout(5000);
            con.setUseCaches( false );
            con.setRequestProperty("Authorization", "Basic " + encodeAuthorization);
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


                StringBuffer responseJSON = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    responseJSON.append(inputLine);
                }
                ObjectMapper mapper = new ObjectMapper();
                Log.w("aaa",responseJSON.toString());
                this.recvResponses = mapper.readValue(responseJSON.toString(), new TypeReference<Map<String,Object>>() {});



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

    public Map<String,Object> sendRequestIndividualBeacon(Map<String,Object> requestDic ,int bid){   //ビーコン毎の情報取得

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS+0900");
        //String Date = calendar.get(Calendar.YEAR)+"-"+"01"+"-"+calendar.get(Calendar.DATE)+"T"+"06"+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"+0900";
        String Date = sdf.format(calendar.getTime());
        String encodeAuthorization = Base64.encodeToString((userID + ":" + password).getBytes(), Base64.NO_WRAP);   //Basic認証に使用
        String HashString = Salt+"tags0time"+Date+"bid"+bid+"serviceid"+serviceid+"subid"+subid;   //ハッシュ化する文字列を生成
        HashString = makeHashString(HashString);    //文字列をハッシュ値に変換
        Map<String,Object> sendRequestDic = new HashMap<String,Object>();
        Object[] tags = new Object[1];
        Map<String,Object> minitags = new HashMap<String,Object>();
        sendRequestDic.put("h",HashString);
        sendRequestDic.put("subid",subid);
        sendRequestDic.put("serviceid",serviceid);
        minitags.put("time",Date);
        minitags.put("bid",bid);
        tags[0] = minitags;
        sendRequestDic.put("tags",tags);

        String requestString = makeJSONString(sendRequestDic);
        Log.w("xxxxxx",requestString);
        HttpsURLConnection con = null;
        URL url = null;
        String urlSt = "";
        urlSt = "https://gw11.clean-beacon.com/bapi/1.0.0/beacon_establish.json";

        try {
            // URLの作成
            url = new URL(urlSt);
            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            // URL接続からデータを読み取る場合はtrue
            con.setDoInput(true);
            // URL接続にデータを書き込む場合はtrue
            con.setDoOutput(true);
            // タイムアウト設定
            con.setConnectTimeout(5000);
            con.setUseCaches( false );
            con.setRequestProperty("Authorization", "Basic " + encodeAuthorization);
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


            StringBuffer responseJSON = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                responseJSON.append(inputLine);
            }
            ObjectMapper mapper = new ObjectMapper();
            Log.w("aaa",responseJSON.toString());
            this.recvResponses = mapper.readValue(responseJSON.toString(), new TypeReference<Map<String,Object>>() {});



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

    private static String makeHashString(String HashString){
        String ALGORISM = "SHA-256"; //生成方式：SHA256



        byte[] cipher_byte;
        try{
            MessageDigest md = MessageDigest.getInstance(ALGORISM);
            md.update(HashString.getBytes());
            cipher_byte = md.digest();
            StringBuilder sb = new StringBuilder(2 * cipher_byte.length);
            for(byte b: cipher_byte) {
                sb.append(String.format("%02x", b&0xff) );
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
