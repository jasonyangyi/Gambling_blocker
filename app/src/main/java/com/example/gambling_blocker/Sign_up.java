package com.example.gambling_blocker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Sign_up extends AppCompatActivity implements View.OnClickListener {

    private TextView tv;
    private EditText inputemail, inputpassword;
    private Button signup;
    private ProgressBar pre;
    // define the UI component
    private FirebaseAuth user;
    // this variable will be used to create a user

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.sign_up);

       user = FirebaseAuth.getInstance();
       Initalize_SigninUI();
       tv.setOnClickListener(this);
       signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView2:
                finish(); //  finish teh current activity/ disappear the current page
                break;
            case R.id.button2:
                // here is the implementation of the sign up function
                User_Sign_up();
                break;

        }
    }

    private void User_Sign_up()
    {
        String email = inputemail.getText().toString().trim(); // get the input of the user
        String password = inputpassword.getText().toString().trim();
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "Please input your email!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(getApplicationContext(), "Please input your password", Toast.LENGTH_LONG).show();
        }
        else if(password.length()<6) // the minimum of password should be 6
        {
            Toast.makeText(getApplicationContext(),"Passord too short! it cannot be fewer than 6 characters",Toast.LENGTH_LONG).show();
        }
        else {
            pre.setVisibility(View.VISIBLE); // use the progress bar to give user the progress feedback
            user.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Sign_up.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pre.setVisibility(View.GONE);  // invoke this method provided by google api to implement the register function
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Sorry, sign up failed" + task.getException(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Sign up successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }
    }
    private void Initalize_SigninUI()
    {
        /*
        Initialize the UI of  this sign up page
         */
        tv=findViewById(R.id.textView2);
        inputemail = (EditText)findViewById(R.id.editText3);
        inputpassword = (EditText) findViewById(R.id.editText4);
        signup = (Button)findViewById(R.id.button2);
        pre = (ProgressBar) findViewById(R.id.progressBar);
    }


    @Override
    protected void onResume() {
        super.onResume();
        pre.setVisibility(View.GONE);
    }
}
