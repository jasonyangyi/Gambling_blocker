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

public class Gambling_exclusion extends Fragment implements View.OnClickListener {

    private Button Start_button;

    public Gambling_exclusion()
    {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.gambling_exclusion,container,false);
         Start_button = (Button) view.findViewById(R.id.button3);
        Start_button.setOnClickListener(this);

        return view ;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button3:
                Intent i1 = new Intent(getActivity(),Gambling_exclusion_setting.class);
                startActivity(i1);
                break;
        }

    }
}
