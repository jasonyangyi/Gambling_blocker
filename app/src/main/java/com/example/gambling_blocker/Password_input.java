package com.example.gambling_blocker;

import android.content.DialogInterface;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_input);
        Initialize_UI();
        Intent intent =getIntent();
        password = intent.getStringExtra("password");
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
                        Remove_exclusion();
                        Close_button();
                        new CountDownTimer(duration, 1000) {

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
                        Toast.makeText(getApplicationContext(),"The new service"+duration+"has been made",Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent("open");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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

    private void ShowExpiredDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Password_input.this);
        builder.setTitle("Reminder");
        builder.setMessage("We are sorry to tell you that the service has been expired");
        builder.setIcon(R.mipmap.logo_gambling_blocker);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
