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
import android.view.MenuItem;
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
        stateProgressBar.setStateDescriptionData(stepdata); // set the description data to the state progress bar
        back = (Button)findViewById(R.id.button7);
        back.setOnClickListener(this);
        back.setAlpha(0.3f);   //  set the alpha of the button
        back.setClickable(false);  // make the button unclickable
        next = (Button)findViewById(R.id.button8);
        next.setOnClickListener(this);
        layout = (LinearLayout) findViewById(R.id.changelayout);
        step1 = (TextView)findViewById(R.id.textView12);
        spinner = (Spinner)findViewById(R.id.spinner);// bind the value with the spinner UI component
        Setspinner();
        spinner.setOnItemSelectedListener(this);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        step2view = inflater.inflate(R.layout.gambling_exclusion_setting2,null);
        step3view = inflater.inflate(R.layout.gambling_exclusion_setting3,null);
        switch_button = (Switch)step3view.findViewById(R.id.switch1);
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = (switch_button).isChecked();
                new CountDownTimer(servicetime, 1000) { // set the time of the service using a CountDownTimer

                    public void onTick(long millisUntilFinished) { }

                    public void onFinish() {
                        /*
                        when the time finish, send the broadcast to stop the service
                         */
                        Intent intent = new Intent("stop");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        /*
                        if the service stops switch off the button
                         */
                        switch_button.setChecked(false);
                        switch_button.setClickable(true);
                        /*
                        display the dialog to tell the user that the service has expired
                         */
                        ShowExpiredDialog();
                    }
                }.start();
                if (on)
                {
                    /*
                    if the state of the button is on, start the VPN service
                     */
                    Intent intent = VpnService.prepare(getApplicationContext());// prepare the user action
                    if(intent!=null)
                    {
                        startActivityForResult(intent,0);
                    }
                    else{
                        onActivityResult(0,RESULT_OK,null);
                    }
                    switch_button.setClickable(false); // make the button unclickable  until the end of the time
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
               },1000);
           }
    }

    private void ProgressbarandNextButton()
    {
        switch (stateProgressBar.getCurrentStateNumber())
        {
            case 1:
                /*
                if the current state number is 1 and user clicks the next button
                add the step2 view
                set the alpha of the button
                 */
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                AddStep2View();
                back.setAlpha(1f);
                back.setClickable(true);
                break;
            case 2:
                /*
                check whether the box is ticked
                if not show user the toast that they need to tick the box
                else reset the state number add the step view
                set the alpha of the button make it look un clickable
                 */
                checkBox = step2view.findViewById(R.id.checkBox);
                if(!checkBox.isChecked())
                {
                    Toast.makeText(getApplicationContext(),"You need to tick the box",Toast.LENGTH_LONG).show();
                }
                else {
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                    AddStep3View();
                    next.setAlpha(0.3f);
                    next.setClickable(false);
                }
                break;
        }
    }

    private void ProgressbarandBackButton()
    {
        switch (stateProgressBar.getCurrentStateNumber())
        {
            case 3:
                /*
                if the current state number is 3 and user clicks the back button
                if the switch button is clicked the has been started cannot return back
                else set the current state number to 2
                invoke the remove view method to remove the step3 view
                add the step2 view
                set the alpha of the button
                make it clickable
                 */
                if(switch_button.isChecked())
                {
                    Toast.makeText(getApplicationContext(),"The service has been enabled,you cannot return",Toast.LENGTH_LONG).show();
                }
                else {
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    layout.removeView(step3view);
                    AddStep2View();
                }
                next.setAlpha(1f);
                next.setClickable(true);
                break;
            case 2:
                /*
                if the current state number is 2 and user clicks the back button
                else set the current state number to 1
                invoke the remove view method to remove the step2 view
                set the step 1 view
                set the alpha of the button
                make it un clickable
                 */
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                    layout.removeView(step2view);
                    DisplaySpinner();
                    back.setAlpha(0.3f);
                    back.setClickable(false);
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
        /*
        make the spinner and text view invisible
         */
        spinner.setVisibility(View.GONE);
        step1.setVisibility(View.GONE);
    }

    private void DisplaySpinner()
    {

        spinner.setVisibility(View.VISIBLE);
        step1.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home)
        {
            // if user click the item, it will act like user press the home button
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        /*
        get the spinner value and transfer it to the service
         */
       spinnervalue = spinner.getSelectedItem().toString();
       Toast.makeText(getApplicationContext(),"You selected "+spinnervalue,Toast.LENGTH_LONG).show();
       servicetime = getServicetime(spinnervalue);
       Toast.makeText(getApplicationContext(),"The duration is "+servicetime,Toast.LENGTH_LONG).show();
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

    private void ShowExpiredDialog()
    {
        /*
        show user the dialog
         set the title, message, icon, positive button of the dialog
         create the dialog
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(Gambling_exclusion_setting.this);
        builder.setTitle("Reminder");
        builder.setMessage("We are sorry to tell you that the service has expired");
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
