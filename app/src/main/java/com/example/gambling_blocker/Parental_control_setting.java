package com.example.gambling_blocker;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kofigyan.stateprogressbar.StateProgressBar;

public class Parental_control_setting extends AppCompatActivity implements View.OnClickListener {
    private Button Back,Next;
    private StateProgressBar stateProgressBar2;
    private EditText setpassword, confirmpassword;
    private TextView setPassword_tv;
    private String[] step = {"Step1","Step2","Step3","Step4"};
    private LayoutInflater inflater;
    private View step2view,step3view,step4view;
    private LinearLayout changelayout2;
    private Spinner spinner2;
    private CheckBox checkBox;
    private Switch switch2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parental_control_setting);
        Intialize_UI();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button9:
                if((TextUtils.isEmpty(setpassword.getText().toString()))||(TextUtils.isEmpty(confirmpassword.getText().toString())))
                {
                    Toast.makeText(getApplicationContext(),"Please set your control password",Toast.LENGTH_LONG).show();
                }
                else if(setpassword.getText().toString().length()<6)
                {
                    Toast.makeText(getApplicationContext(),"The minimun length of password should be six",Toast.LENGTH_LONG).show();
                }
                else if (!Isthesamepassword()){
                    Toast.makeText(getApplicationContext(),"Make sure the password are same!!!",Toast.LENGTH_LONG).show();
                }
                else {
                    Progressbar_NextBar();
                }
                break;
            case R.id.button10:
                Progressbar_BackBar();
                break;
        }
    }

    private void Intialize_UI()
    {
        Back = (Button)findViewById(R.id.button10);
        Next = (Button)findViewById(R.id.button9);
        Back.setOnClickListener(this);
        Next.setOnClickListener(this);
        stateProgressBar2 = (StateProgressBar)findViewById(R.id.second_progress_bar);
        stateProgressBar2.setStateDescriptionData(step);
        setpassword = (EditText)findViewById(R.id.editText9);
        confirmpassword = (EditText)findViewById(R.id.editText10);
        setPassword_tv = (TextView)findViewById(R.id.setpassword);
        changelayout2 = (LinearLayout)findViewById(R.id.changelayout2);
        inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        step2view = inflater.inflate(R.layout.parental_control_seting2,null);
        step3view = inflater.inflate(R.layout.parental_control_setting3,null);
        step4view = inflater.inflate(R.layout.parental_control_setting4,null);
        switch2 = (Switch)step4view.findViewById(R.id.switch2);
        switch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = (switch2).isChecked();
                if (on)
                {
                    Intent intent = VpnService.prepare(getApplicationContext());// prepare the user action
                    if(intent!=null)
                    {
                        startActivityForResult(intent,0);
                    }
                    else{
                        onActivityResult(0,RESULT_OK,null);
                    }
                }
                else {
                    //  stopService(new Intent(getApplicationContext(),Gambling_Block_Service.class));
                    Intent intent = new Intent("stop");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }
        });
        spinner2 = (Spinner)step2view.findViewById(R.id.spinner2);
        Setspinner();
    }

    private boolean Isthesamepassword()
    {
        boolean judge = false;
        if(setpassword.getText().toString().equals(confirmpassword.getText().toString())) {
            judge = true;
        }
        else{
            judge = false;
        }
        return judge;
    }

    private void Setspinner()
    {
        ArrayAdapter<CharSequence> timeadapter = ArrayAdapter.createFromResource(this,R.array.exclusion_duration,R.layout.support_simple_spinner_dropdown_item);
        timeadapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner2.setAdapter(timeadapter);
    }

    private void Progressbar_NextBar()
    {
        switch (stateProgressBar2.getCurrentStateNumber())
        {
            case 1:
                stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                Hideview1();
                changelayout2.addView(step2view);
                break;
            case 2:
                stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                changelayout2.removeView(step2view);
                changelayout2.addView(step3view);
                break;
            case 3:
                checkBox = (CheckBox)step3view.findViewById(R.id.checkBox3);
                if(!checkBox.isChecked())
                {
                   Toast.makeText(getApplicationContext(),"You need to tick the box",Toast.LENGTH_LONG).show();
                }
                else{
                    changelayout2.removeView(step3view);
                    changelayout2.addView(step4view);
                    stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                }
                break;
        }
    }

    private void Progressbar_BackBar()
    {
        switch (stateProgressBar2.getCurrentStateNumber())
        {
            case 4:
                if(switch2.isChecked())
                {
                  Toast.makeText(getApplicationContext(),"The function has been enabled, you cannot return",Toast.LENGTH_LONG).show();
                }
                else
                    {
                    stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                    changelayout2.removeView(step4view);
                    changelayout2.addView(step3view);
                }
                break;
            case 3:
                stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                changelayout2.removeView(step3view);
                changelayout2.addView(step2view);
                break;
            case 2:
                stateProgressBar2.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                changelayout2.removeView(step2view);
                Displayview1();
                break;
        }
    }

    private void Hideview1() // hide the step1 view
    {
        setPassword_tv.setVisibility(View.GONE);
        setpassword.setVisibility(View.GONE);
        confirmpassword.setVisibility(View.GONE);
    }

    private void Displayview1() // display the step1 view
    {
        setPassword_tv.setVisibility(View.VISIBLE);
        setpassword.setVisibility(View.VISIBLE);
        confirmpassword.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK)
        {
            startService( new Intent(getApplicationContext(),Gambling_Block_Service.class));
        }
    }
}
