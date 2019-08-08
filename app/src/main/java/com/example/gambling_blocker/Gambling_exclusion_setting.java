package com.example.gambling_blocker;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kofigyan.stateprogressbar.StateProgressBar;

import org.w3c.dom.Text;

public class Gambling_exclusion_setting extends AppCompatActivity implements View.OnClickListener {

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
        spinner = (Spinner)findViewById(R.id.spinner);
        inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        step2view = inflater.inflate(R.layout.gambling_exclusion_setting2,null);
        step3view = inflater.inflate(R.layout.gambling_exclusion_setting3,null);
        switch_button = (Switch)step3view.findViewById(R.id.switch1);
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = (switch_button).isChecked();
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
        Setspinner();
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

}
