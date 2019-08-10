package com.example.gambling_blocker;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.kofigyan.stateprogressbar.StateProgressBar;

public class Gambling_exclusion_setting extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private String[] stepdata = {"Step 1","Step 2","Step3"};
    private Switch switch_button;
    private StateProgressBar stateProgressBar;
    private Button back,next;
    private LinearLayout layout;
    private Spinner spinner;
    private LayoutInflater inflater;
    private TextView step2,step1;
    private CheckBox checkBox;
    private View step2view,step3view;
    private String spinnervalue;
    private long servicetime;
    private CountDownTimer timer;
    private AlertDialog dialog;
    private String servicename = "Gambling exclusion";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gambling_exclusion_setting);
        Inlitizae_UI();
    }

    private void Inlitizae_UI()
    {
        stateProgressBar = (StateProgressBar)findViewById(R.id.your_state_progress_bar_id);
        stateProgressBar.setStateDescriptionData(stepdata);
        back = (Button)findViewById(R.id.button7);
        back.setOnClickListener(this);
        next = (Button)findViewById(R.id.button8);
        next.setOnClickListener(this);
        layout = (LinearLayout) findViewById(R.id.changelayout);
        step1 = (TextView)findViewById(R.id.textView12);
        spinner = (Spinner)findViewById(R.id.spinner);// bind the value with the spinner UI component
        Setspinner();
        spinner.setOnItemSelectedListener(this);
        inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        step2view = inflater.inflate(R.layout.gambling_exclusion_setting2,null);
        step3view = inflater.inflate(R.layout.gambling_exclusion_setting3,null);
        switch_button = (Switch)step3view.findViewById(R.id.switch1);
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = (switch_button).isChecked();
                new CountDownTimer(servicetime, 1000) {

                    public void onTick(long millisUntilFinished) { }

                    public void onFinish() {
                        Intent intent = new Intent("stop");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        switch_button.setChecked(false);
                        switch_button.setClickable(true);
                        ShowExpiredDialog();
                    }
                }.start();
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
                    switch_button.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button7: // when click the back button
                ProgressbarandBackButton();
                break;
            case R.id.button8: // when click the next button
                ProgressbarandNextButton();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
           if(resultCode==RESULT_OK)
           {
               startService( new Intent(getApplicationContext(),Gambling_Block_Service.class));
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                   Intent i1 = new Intent(getApplicationContext(),Successful_configuration.class);
                       i1.putExtra("Name",servicename);
                       i1.putExtra("Duration",servicetime);
                   startActivity(i1);
                   }
               },5000);

           }
    }

    private void ProgressbarandNextButton()
    {
        switch (stateProgressBar.getCurrentStateNumber())
        {
            case 1:
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                AddStep2View();
                break;
            case 2:
                checkBox = step2view.findViewById(R.id.checkBox);
                if(!checkBox.isChecked())
                {
                    Toast.makeText(getApplicationContext(),"You need to tick the box",Toast.LENGTH_LONG).show();
                }
                else {
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                    AddStep3View();
                }
                break;
        }
    }

    private void ProgressbarandBackButton()
    {
        switch (stateProgressBar.getCurrentStateNumber())
        {
            case 3:
                if(switch_button.isChecked())
                {
                    Toast.makeText(getApplicationContext(),"The service has been enabled,you cannot return",Toast.LENGTH_LONG).show();
                }
                else {
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    layout.removeView(step3view);
                    AddStep2View();
                }
                break;
            case 2:
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                    layout.removeView(step2view);
                    DisplaySpinner();
                break;
        }
    }

    private void Setspinner()
    {
        ArrayAdapter<CharSequence> timeadapter = ArrayAdapter.createFromResource(this,R.array.exclusion_duration,R.layout.support_simple_spinner_dropdown_item);
        timeadapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(timeadapter);
    }

    private void AddStep2View()
    {
        step2 = step2view.findViewById(R.id.textView14);
        HideSpinner();
        layout.addView(step2view);
    }

    private void AddStep3View()
    {
        layout.removeView(step2view);
        layout.addView(step3view);

    }

    private void HideSpinner()
    {
        spinner.setVisibility(View.GONE);
        step1.setVisibility(View.GONE);
    }

    private void DisplaySpinner()
    {
        spinner.setVisibility(View.VISIBLE);
        step1.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
       spinnervalue = spinner.getSelectedItem().toString();
       Toast.makeText(getApplicationContext(),"You selected "+spinnervalue,Toast.LENGTH_LONG).show();
       servicetime = getServicetime(spinnervalue);
       Toast.makeText(getApplicationContext(),"The duration is "+servicetime,Toast.LENGTH_LONG).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Gambling_exclusion_setting.this);
        builder.setTitle("Reminder");
        builder.setMessage("We are sorry to tell you that the service has been expired");
        builder.setIcon(R.mipmap.logo_gambling_blocker);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                  dialogInterface.cancel();
            }
        });
         dialog = builder.create();
        dialog.show();
    }
}
