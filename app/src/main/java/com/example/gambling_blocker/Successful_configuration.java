package com.example.gambling_blocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Successful_configuration extends AppCompatActivity implements View.OnClickListener {

    private String Service;
    private TextView tvtitle,tvinformation;
    private Button edit,send,cancel;
    private CheckBox password,gambling_box;
    private String service;
    private long duration;
    private String duration2;
    private String key;
    private ImageButton post_website;
    private EditText website_input, your_message;
    private DatabaseReference postDatabase;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.successful_configuration);
        postDatabase = FirebaseDatabase.getInstance().getReference("Post");// get the reference of a database
     Initialize_UI();
     Intent intent = getIntent();
     // use getStringExtra method to get the passed value through the intent
     service = intent.getStringExtra("Name");
     duration = intent.getLongExtra("Duration",0);
     key = intent.getStringExtra("Key");
     duration2 = getDuration(duration);
     if(service.equals("Gambling exclusion")){
         tvinformation.setText("Restriction will be lifted in "+duration2);
        edit.setClickable(false);
        edit.setAlpha(0.2f);
        /*
        if the service is gambling exclusion
        set the alpha of the button make it look like disabled
         */
     }
      if(service.equals("Parental Control")){
         tvinformation.setText("Restriction will be lifted in "+duration2);
         password.setVisibility(View.VISIBLE);
         tvtitle.setText("Parental Control");
         /*
         if the service is parental control
         the page will look like this
          */
     }
    }

    private void Initialize_UI()
    {
        tvtitle = (TextView)findViewById(R.id.textView_title);
        tvinformation = (TextView)findViewById(R.id.textView20);
        // enable the home button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edit = (Button) findViewById(R.id.button_edit);
        edit.setOnClickListener(this);
        password = (CheckBox)findViewById(R.id.checkBox4);
        password.setClickable(false);
        gambling_box = (CheckBox)findViewById(R.id.checkBox2);
        gambling_box.setClickable(false);
        post_website = (ImageButton)findViewById(R.id.imageButton2);
        post_website.setOnClickListener(this);

    }

    private String getDuration(long time)
    {
        /*
        this method is used to transfer the long value to the string value
         */
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
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button_edit:
                /*
                when user click the edit button
                invoke the startActivity method to
                start the password input page
                 */
                Intent intent = new Intent(getApplicationContext(),Password_input.class);
                intent.putExtra("password",key);
                startActivity(intent);
                finish();
                break;
            case R.id.imageButton2:
                /*
                press this button to show the dialog
                 */
                Create_dialog();
                break;
        }
    }

    private void Write_to_Postdatabase(String address,String message)
    {/*
    insert the data information to the database
      */
        Website_post post = new Website_post(address,message);
        postDatabase.child("Post").push().setValue(post);
    }



    private void Create_dialog()
    {
        /*
        use the custom dialog view
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(Successful_configuration.this);
        View dialog_view = getLayoutInflater().inflate(R.layout.post_website_to_block,null);
        website_input = (EditText)dialog_view.findViewById(R.id.editText_website);
        your_message  = (EditText)dialog_view.findViewById(R.id.editText_your_message);
        send = (Button)dialog_view.findViewById(R.id.button_send);
        cancel = (Button)dialog_view.findViewById(R.id.button_cancel);
        builder.setView(dialog_view);
        final AlertDialog post_dialog = builder.create();
        post_dialog.show();cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            post_dialog.cancel();
        }
    });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                get the input of the user
                write them to the realtime database provided by the Google api
                 */
                String address = website_input.getText().toString();
                String message = your_message.getText().toString();
                Write_to_Postdatabase(address,message);
                post_dialog.cancel();
            }
        });


    }

}
