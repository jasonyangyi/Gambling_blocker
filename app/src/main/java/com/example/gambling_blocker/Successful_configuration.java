package com.example.gambling_blocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Successful_configuration extends AppCompatActivity {

    private String Service;
    private TextView tvtitle,tvinformation;
    private Button edit;
    private CheckBox password;
    private String service;
    private long duration;
    private String duration2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.successful_configuration);
     Initialize_UI();
     Intent intent = getIntent();
     service = intent.getStringExtra("Name");
     duration = intent.getLongExtra("Duration",0);
     duration2 = getDuration(duration);
     if(service.equals("Gambling exclusion")){
         tvinformation.setText("Restriction will be lifted in "+duration2);
        edit.setClickable(false);
     }
      if(service.equals("Parental Control")){
         tvinformation.setText("Restriction will be lifted in "+duration2);
         password.setVisibility(View.VISIBLE);
         tvtitle.setText("Parental Control");
     }
    }

    private void Initialize_UI()
    {
        tvtitle = (TextView)findViewById(R.id.textView_title);
        tvinformation = (TextView)findViewById(R.id.textView20);
        edit = (Button) findViewById(R.id.button_edit);
        password = (CheckBox)findViewById(R.id.checkBox4);
    }

    private String getDuration(long time)
    {
        String value = null;
        if(time==10000)
        {
            value = "10s";
        }
        else if(time==86400000){
            value = "24h";
        }
        else if(time==604800000){
            value = "one week";
        }
        else if(time == 2592000000l){
            value = "one month";
        }
        else if(time == 15552000000l){
            value = "six months";
        }
        else if(time == 31104000000l){
            value = "one year";
        }

        return value;
    }



}
