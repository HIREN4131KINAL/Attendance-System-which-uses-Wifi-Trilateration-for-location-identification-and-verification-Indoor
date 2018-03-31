package com.creatershub.attendancesystemteacherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class AttendanceProgress extends AppCompatActivity {
    String subjectId, attendanceId, responseArray;
    final String url = "https://attendance-system-archdj.herokuapp.com/";
    RequestQueue requestQueue;

    void stopAttendance(final Boolean cancelled) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", attendanceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deleteUrl = url + "ongoingattendance/" + attendanceId + "/";
        final ProgressDialog dialog = new ProgressDialog(AttendanceProgress.this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.DELETE, deleteUrl, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        if (cancelled) {
                            Toast.makeText(AttendanceProgress.this, "Attendance has been cancelled!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                            startActivity(intent);
                            finish();
                        }

                        else {
                            Intent intent = new Intent(getApplicationContext(), MarkAttendance.class);
                            intent.putExtra("studentList", responseArray);
                            intent.putExtra("subjectId", subjectId);
                            startActivity(intent);
                            finish();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(AttendanceProgress.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(AttendanceProgress.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    public void onClickCancelAttendance(View view) {
        stopAttendance(true);
    }

    public void onClickFinishAttendance(View view) {
        stopAttendance(false);
    }

    void fetchStudentList() {
        Toast.makeText(this, "Fetching list of students...", Toast.LENGTH_SHORT).show();
        String getStudentsUrl = url + "getstudents/" + subjectId + "/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, getStudentsUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Toast.makeText(AttendanceProgress.this, "List of students fetched!", Toast.LENGTH_SHORT).show();
                        responseArray = response.toString();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Log.e("Error hai", error.getMessage());
                            Toast.makeText(AttendanceProgress.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(AttendanceProgress.this, "Internal server error. Can't fetch students list!", Toast.LENGTH_SHORT).show();
                        }
                        Button cancelButton = findViewById(R.id.cancelAttendanceButton);
                        cancelButton.performClick();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_progress);
        Intent intent = getIntent();
        attendanceId = intent.getStringExtra("attendanceId");
        subjectId = intent.getStringExtra("subjectID");
        requestQueue = Volley.newRequestQueue(this);
        fetchStudentList();
    }
}
