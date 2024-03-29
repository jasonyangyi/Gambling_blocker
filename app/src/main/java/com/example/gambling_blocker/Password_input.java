package com.example.gambling_blocker;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class Password_input extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Button back,save;
    private String password; //  get the input password
    private RadioButton change,remove;
    private Spinner spinner;
    private EditText password_edit;
    private String parent_key;
    private String spinnervalue;
    private long duration;
    private AlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_input);
        Initialize_UI();
        Intent intent =getIntent();
        password = intent.getStringExtra("password");// use getStringExtra method to get the passed value
        Toast.makeText(getApplicationContext(),"the key is "+password,Toast.LENGTH_LONG).show();
    }

    private void Initialize_UI()
    {
        back = (Button)findViewById(R.id.button_back);
        back.setOnClickListener(this);
        change = (RadioButton)findViewById(R.id.change_restriction);
        remove = (RadioButton)findViewById(R.id.remove_restriction);
        spinner = (Spinner)findViewById(R.id.spinner3);
        Setspinner();
        spinner.setOnItemSelectedListener(this);
        password_edit = (EditText)findViewById(R.id.editText_key);
        save = (Button)findViewById(R.id.button_save);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button_back:
                finish();
                break;
            case R.id.button_save:
                /*
                get the input from the user
                compare this with the passed password

                 */
                parent_key = password_edit.getText().toString();
                if(parent_key==null)
                {
                    Toast.makeText(getApplicationContext(),"Please input the password!",Toast.LENGTH_LONG).show();
                }
                else if(!(parent_key.equals(password)))
                {
                    Toast.makeText(getApplicationContext(),"Wrong password, please input again!",Toast.LENGTH_LONG).show();
                }
                else if(parent_key.equals(password))
                {
                    if(change.isChecked()==true)
                    { // change the exclusion
                        Remove_exclusion();// first stop the service
                        Close_button();   // send the button to switch the button off

                        new Handler().postDelayed(new Runnable() { // use this handler to delay the action for about 3 seconds
                            @Override
                            public void run() {
                                new CountDownTimer(duration, 1000) {
                                  // a new counter to count again
                                    public void onTick(long millisUntilFinished) { }

                                    public void onFinish() {
                                        Remove_exclusion();
                                        Close_button();
                                    }
                                }.start();
                                Intent intent = VpnService.prepare(getApplicationContext());// prepare the user action
                                if(intent!=null)
                                {
                                    startActivityForResult(intent,0);
                                }
                                else{
                                    onActivityResult(0,RESULT_OK,null);
                                }

                                Open_button();
                            }
                        },3000);
                        finish();
                    }
                    if(remove.isChecked()==true)
                    { // remove the exclusion
                        Remove_exclusion();
                        Close_button();
                        finish(); //  finish the current activity
                    }
                }
                break;
        }
    }

    private void Setspinner()
    {
         /*
        use array adapter to store the resource value
        use setDropDownViewResource method to set the value to the spinner
         */
        ArrayAdapter<CharSequence> timeadapter = ArrayAdapter.createFromResource(this,R.array.exclusion_duration,R.layout.support_simple_spinner_dropdown_item);
        timeadapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(timeadapter);
    }

    private void Remove_exclusion()
    {
        Intent intent = new Intent("stop"); // first stop the service
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void Close_button()
    {

        Intent intent = new Intent("close"); // the push the button to the off state
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }



    private void Open_button()
    {
        Intent intent = new Intent("open"); // send the broadcast to switch the button on
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
         /*
        get the spinner value and transfer it to the service
         */
       spinnervalue = spinner.getSelectedItem().toString();
       duration = getServicetime(spinnervalue);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK)
        {
            startService(new Intent(getApplicationContext(), Gambling_Block_Service.class));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }
    private long getServicetime(String value)
    {
         /*
        transfer the String value to long value to get the service time
         */
        long servicetime = 0;
        if(value.equals("10s"))
        {
            servicetime = 10000;
        }
        else if(value.equals("24h")){
            servicetime = 86400000;
        }
        else if(value.equals("one week")){
            servicetime = 604800000;
        }
        else if(value.equals("one month")){
            servicetime = 2592000000l;
        }
        else if(value.equals("six months")){
            servicetime = 15552000000l;
        }
        else if(value.equals("one year")){
            servicetime = 31104000000l;
        }
        return servicetime;
    }



}
