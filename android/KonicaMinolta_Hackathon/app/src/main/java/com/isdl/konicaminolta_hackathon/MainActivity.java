package com.isdl.konicaminolta_hackathon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.isdl.konicaminolta_hackathon.R.id.ToRecognizer;
import static com.isdl.konicaminolta_hackathon.R.id.ToSleeper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HttpAsync getAsync= new HttpAsync();

        Button toSleeper = (Button)findViewById(ToSleeper);
        Button toRecognizer = (Button)findViewById(ToRecognizer);

        toSleeper.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                System.out.println("あああああああああああああああああ");

                //params[0]：endpoint , params[1]：GET or POST
                String[] params = { "/", "GET" };
                getAsync.execute(params);

//                ここに寝たい人へのintent遷移or寝たい情報の送信処理
            }
        });



        toRecognizer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaa");
//                ここに承認する人へのintent遷移or承認の送信処理
            }
        });
    }
}
