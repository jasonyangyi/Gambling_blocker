package com.example.gambling_blocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Parental_control extends Fragment implements View.OnClickListener {
    /*
    this fragment displays the view of the parental control
     */
    private Button start_button;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parental_control,container,false);
        start_button = (Button)view.findViewById(R.id.button4);
        start_button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button4:
                Intent i1 = new Intent(getActivity(),Parental_control_setting.class);
                startActivity(i1); // use intent to start the parental control module
                break;
        }
    }
}
