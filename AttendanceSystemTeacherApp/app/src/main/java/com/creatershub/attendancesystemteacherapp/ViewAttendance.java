package com.creatershub.attendancesystemteacherapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewAttendance extends AppCompatActivity {
    String subjectId;
    JSONArray studentListJsonArray;
    ListView studentListView;
    ArrayList<Student> studentList;
    int subjectPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);
        Intent intent = getIntent();
        studentListView = findViewById(R.id.studentListView);
        subjectId = intent.getStringExtra("subjectID");
        try {
            studentListJsonArray = new JSONArray(intent.getStringExtra("studentList"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locateSubject();
        populateList();
        StudentListAdapter adapter = new StudentListAdapter(this, R.layout.adapter_view_layout, studentList);
        studentListView.setAdapter(adapter);
    }

    void locateSubject() {
        try {
            JSONObject student = studentListJsonArray.getJSONObject(0);
            JSONArray subjects = student.getJSONArray("subjects");

            for (int i = 0; i < subjects.length(); i++) {
                JSONObject obj = subjects.getJSONObject(i);
                JSONObject subject = obj.getJSONObject("subject");
                String id = subject.getString("_id");

                if (id.equals(subjectId)) {
                    subjectPosition = i;
                    String totalClasses = Integer.toString(subject.getInt("total_classes"));
                    TextView totalClassesTextView = findViewById(R.id.viewAttendanceTotalClassesTextView);
                    totalClassesTextView.setText(String.format("Total classes = %s", totalClasses));
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void populateList() {
        studentList = new ArrayList<>();

        for (int i = 0; i < studentListJsonArray.length(); i++) {
            try {
                JSONObject student = studentListJsonArray.getJSONObject(i);
                String name = student.getString("name");
                String roll = student.getString("roll");
                JSONArray subjects = student.getJSONArray("subjects");
                JSONObject subject = subjects.getJSONObject(subjectPosition);
                String attendance = Integer.toString(subject.getInt("attendance"));

                Student studentObj = new Student(roll, name, attendance);
                studentList.add(studentObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
