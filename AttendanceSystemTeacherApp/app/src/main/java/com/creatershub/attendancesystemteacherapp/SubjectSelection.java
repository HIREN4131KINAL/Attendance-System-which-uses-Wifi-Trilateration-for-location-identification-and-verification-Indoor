package com.creatershub.attendancesystemteacherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectSelection extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    JSONArray subjectList;
    List<String> list;
    String responseArray, subjectId, attendanceId, operation, url = "https://attendance-system-archdj.herokuapp.com/";
    TextView subjectCodeTextView, subjectNameTextView, subjectBranchTextView, subjectSemTextView, subjectRoomTextView;

    public void onClickSubjectSelected(View view) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // If Start Attendance is invoked
        if (operation.equals("StartAttendance")) {
            final JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("subject", subjectId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final ProgressDialog dialog = new ProgressDialog(SubjectSelection.this);
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
            String startAttendanceUrl = url + "ongoingattendance/";

            JsonObjectRequest jsonObjectRequest =
                    new JsonObjectRequest(Request.Method.POST, startAttendanceUrl, jsonBody, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            dialog.dismiss();
                            try {
                                attendanceId = response.getString("_id");
                                Toast.makeText(SubjectSelection.this, "Attendance has started!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), AttendanceProgress.class);
                                intent.putExtra("attendanceId", attendanceId);
                                intent.putExtra("subjectID",subjectId);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse == null) {
                                Toast.makeText(SubjectSelection.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                            }

                            else if (error.networkResponse.statusCode == 500) {
                                Toast.makeText(SubjectSelection.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        }


        // If View Attendance is invoked
        else {
            final ProgressDialog dialog = new ProgressDialog(SubjectSelection.this);
            dialog.setMessage("Fetching list of students...");
            dialog.setCancelable(false);
            dialog.show();
            String getStudentsUrl = url + "getstudents/" + subjectId + "/";

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, getStudentsUrl, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            dialog.dismiss();
                            responseArray = response.toString();
                            Intent intent = new Intent(getApplicationContext(), ViewAttendance.class);
                            intent.putExtra("studentList", responseArray);
                            intent.putExtra("subjectID",subjectId);
                            startActivity(intent);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse == null) {
                                Log.e("Error hai", error.getMessage());
                                Toast.makeText(SubjectSelection.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                            }

                            else if (error.networkResponse.statusCode == 500) {
                                Toast.makeText(SubjectSelection.this, "Internal server error. Can't fetch students list!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

            requestQueue.add(jsonArrayRequest);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);
        Intent intent = getIntent();
        operation = intent.getStringExtra("operation");
        subjectCodeTextView = findViewById(R.id.subjectCodeTextView);
        subjectNameTextView = findViewById(R.id.subjectNameTextView);
        subjectBranchTextView = findViewById(R.id.subjectBranchTextView);
        subjectSemTextView = findViewById(R.id.subjectSemTextView);
        subjectRoomTextView = findViewById(R.id.subjectRoomTextView);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TeacherDetails", 0);
        list = new ArrayList<>();
        try {
            subjectList = new JSONArray(sharedPreferences.getString("subjects", ""));
            for (int i = 0; i < subjectList.length(); i++) {
                JSONObject subject = subjectList.getJSONObject(i);
                String sub_name = subject.getString("sub_name");
                String sub_code = subject.getString("code");
                list.add(sub_code + " " + sub_name);
            }

        } catch (JSONException e) {
            Log.e("Error", "In reading subjects");
            e.printStackTrace();
        }
        Spinner subjectListSpinner = findViewById(R.id.subjectListSpinner);
        subjectListSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectListSpinner.setAdapter(dataAdapter);

        try {
            JSONObject subject = subjectList.getJSONObject(0);
            subjectCodeTextView.setText(String.format("Subject Code - %s", subject.getString("code")));
            subjectNameTextView.setText(String.format("Subject Name - %s", subject.getString("sub_name")));
            subjectBranchTextView.setText(String.format("Branch - %s", subject.getString("branch")));
            subjectSemTextView.setText(String.format("Semester - %s", subject.getString("semester")));
            JSONObject room = subject.getJSONObject("room");
            subjectRoomTextView.setText(String.format("Classroom - %s", room.getString("class_name")));
            subjectId = subject.getString("_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            JSONObject subject = subjectList.getJSONObject(position);
            subjectCodeTextView.setText(String.format("Subject Code - %s", subject.getString("code")));
            subjectNameTextView.setText(String.format("Subject Name - %s", subject.getString("sub_name")));
            subjectBranchTextView.setText(String.format("Branch - %s", subject.getString("branch")));
            subjectSemTextView.setText(String.format("Semester - %s", subject.getString("semester")));
            JSONObject room = subject.getJSONObject("room");
            subjectRoomTextView.setText(String.format("Classroom - %s", room.getString("class_name")));
            subjectId = subject.getString("_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}
