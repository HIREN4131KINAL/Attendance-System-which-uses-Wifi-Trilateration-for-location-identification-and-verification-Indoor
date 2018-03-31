package com.creatershub.attendancesystemteacherapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    TextView nameTextView, deptTextView;
    String name, dept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTextView = findViewById(R.id.nameTextView);
        deptTextView = findViewById(R.id.deptTextView);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TeacherDetails", 0);
        name = sharedPreferences.getString("name", "");
        dept = sharedPreferences.getString("department", "");

        nameTextView.setText(String.format("Name - %s", name));
        deptTextView.setText(String.format("Department - %s", dept));
    }
}
