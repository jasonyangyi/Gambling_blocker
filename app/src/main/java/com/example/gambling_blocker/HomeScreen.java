package com.example.gambling_blocker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewpager;
    private FirebaseAuth user;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        Inlitialize_Home_screen();// this function is used to inlitialize the UI of  home screen
            user = FirebaseAuth.getInstance();
    }

    private void setUpViewpager(ViewPager viewpager)
    {
        ViewpagerAdapter viewpagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());
        viewpagerAdapter.addFragment(new Gambling_exclusion(),"Gambling exclusion");
        viewpagerAdapter.addFragment(new Parental_control(),"Parental control");
        viewpager.setAdapter(viewpagerAdapter);
    }

    private void Inlitialize_Home_screen()
    {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        viewpager = (ViewPager)findViewById(R.id.viewpager);
        setUpViewpager(viewpager);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.getTabAt(0).setIcon(R.mipmap.gambling_exclusion);
        tabLayout.getTabAt(1).setIcon(R.mipmap.parental_control);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId())
       {
           case R.id.action_Sign_out:
               User_signout();
               return true;
           default:
               return super.onOptionsItemSelected(item);
       }
    }

    private void User_signout()
    {
        user.signOut();
        finish();
    }

}
