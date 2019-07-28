package com.example.gambling_blocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

public class Login_in extends AppCompatActivity implements View.OnClickListener {

    private TextView tv,tv2;
    private EditText inputEmail,inputPassword,useremail;
    private Button login_button,sendButton,backButton;
    private ProgressBar pre,pre2;
    private FirebaseAuth user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_in);

        user = FirebaseAuth.getInstance();

       Initialize_LoginUI();
        Addlistener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textView:
                Intent i1 = new Intent(getApplicationContext(), Sign_up.class);
                startActivity(i1);
                break;
            case R.id.button:
                User_Login_in();
                break;
            case R.id.textView3:
                AlertDialog.Builder builder = new AlertDialog.Builder(Login_in.this);
                View dialog_view = getLayoutInflater().inflate(R.layout.resetpassword_dialog,null);
                 useremail = (EditText)dialog_view.findViewById(R.id.editText5);
                 sendButton = (Button)dialog_view.findViewById(R.id.button5);
                 backButton = (Button)dialog_view.findViewById(R.id.button6);
                 pre2 = (ProgressBar)dialog_view.findViewById(R.id.progressBar3);
                 builder.setView(dialog_view);
                 final AlertDialog resetpassword_dialog = builder.create();
                 resetpassword_dialog.show();
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    resetpassword_dialog.cancel();
                    }
                });
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String emailaddress = useremail.getText().toString().trim();
                        if(TextUtils.isEmpty(emailaddress))
                        {
                            Toast.makeText(getApplicationContext(),"Please input your email!",Toast.LENGTH_LONG).show();
                        }
                        pre2.setVisibility(View.VISIBLE);
                        user.sendPasswordResetEmail(emailaddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pre2.setVisibility(View.GONE);
                                if(!task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(),"Failed to send the email",Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),"emailed sent successfully",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

    private  void User_Login_in()
    {
        String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(),"Please input your email address",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(getApplicationContext(),"Please input your password",Toast.LENGTH_LONG).show();
        }
        else
        {
            pre.setVisibility(View.VISIBLE);
            user.signInWithEmailAndPassword(email,password).addOnCompleteListener(Login_in.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pre.setVisibility(View.GONE);
                    if(!task.isSuccessful())
                    {
                        if(password.length()<6)
                        {
                            Toast.makeText(getApplicationContext(),"The minimum should be 6 characters",Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Login in successfully",Toast.LENGTH_LONG).show();
                        Intent i1 = new Intent(getApplicationContext(),HomeScreen.class);
                        startActivity(i1);
                    }
                }
            });
        }
    }

    private void Initialize_LoginUI()
    {
        tv = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView3);
        inputEmail = (EditText)findViewById(R.id.editText);
        inputPassword = (EditText)findViewById(R.id.editText2);
        login_button = (Button)findViewById(R.id.button);
        pre = (ProgressBar)findViewById(R.id.progressBar2);
    }

    private void Addlistener()
    {
        tv.setOnClickListener(this);
        tv2.setOnClickListener(this);
        login_button.setOnClickListener(this);

    }
}
