package com.creatershub.attendancesystemteacherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Dashboard extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        TextView dashboardWelcomeTextView = findViewById(R.id.dashboardWelcomeText);
        sharedPreferences = getApplicationContext().getSharedPreferences("TeacherDetails", 0);
        String name = sharedPreferences.getString("name", "");
        String welcomeText = "Welcome " + name + "!";
        dashboardWelcomeTextView.setText(welcomeText);
    }

    public void onClickLogout(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TeacherDetails", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    public void onClickStartAttendance(View view) {
        Intent intent = new Intent(getApplicationContext(), SubjectSelection.class);
        intent.putExtra("operation", "StartAttendance");
        startActivity(intent);
    }

    public void onClickViewAttendance(View view) {
        Intent intent = new Intent(getApplicationContext(), SubjectSelection.class);
        intent.putExtra("operation", "ViewAttendance");
        startActivity(intent);
    }

    public void onClickProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), Profile.class);
        startActivity(intent);
    }
}
